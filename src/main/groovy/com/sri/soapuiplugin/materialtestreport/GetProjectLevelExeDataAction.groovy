package com.sri.soapuiplugin.materialtestreport

'''
author      :   Sridhar Easwaran
twitter     :   @madhu_sridhar
email       :   sridhar.1788@gmail.com

Thanks to:
==========
->  Alvin Wang,Alan Chang,Alex Mark,Kevin Louie - The wonderful team behind "MATERIALIZE CSS"
    www.materializecss.com
->  Fellow front-end dev/designers at "Stackoverflow"
->  Fantastic scripting language - "Groovy"
->  Moreover the "SOAPUI" community 

'''

import java.lang.*
import java.util.*
import java.awt.Desktop
import java.io.*
import java.net.*
import groovy.xml.*
import com.eviware.soapui.SoapUI
import com.eviware.soapui.support.UISupport
import com.eviware.soapui.model.project.Project
import com.eviware.soapui.support.action.support.AbstractSoapUIAction

class GetProjectLevelExeDataAction extends AbstractSoapUIAction <Project>
{

    public GetProjectLevelExeDataAction()
    {
        super("Bake Test-Execution Report", "I Generate Test-Execution Report at Project Level !! ")
    }

    @Override
    void perform(Project project, Object o)
    {

        def proj_name=project.getName()
        def writer = new StringWriter()
        def wp = new groovy.xml.MarkupBuilder(writer)

        //report name
        def c_name = "My Company"
        def p_name = "Lovely Project Report"
        if(project.hasProperty("Report_CompanyName"))
        {
            c_name = project.getPropertyValue("Report_CompanyName")
        }
        else
        {
            project.setPropertyValue("Report_CompanyName",c_name)
        }
        if(project.hasProperty("Report_ProjectName"))
        {
            p_name=project.getPropertyValue("Report_ProjectName")
        }
        else
        {
            project.setPropertyValue("Report_ProjectName",p_name)
        }



        wp.html{

            comment {   '''
                         * SoapUI Material Report
                         * Version: 1.0.1
                         *
                         * Copyright 2015 Sridhar Easwaran
                         * Released under the MIT license
                         * https://github.com/sridhareaswaran/../master/LICENSE.md
                        '''
                    }

            head{
                title project.getName()
                link(rel:'stylesheet',type:'text/css',href:'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.4.0/css/font-awesome.css')
                link(rel:'stylesheet',type:'text/css',href:'https://cdnjs.cloudflare.com/ajax/libs/materialize/0.97.0/css/materialize.min.css')
                link(rel:'stylesheet',href:'https://fonts.googleapis.com/icon?family=Material+Icons')
                style(type:"text/css", '''
                    h5:before {
                      display: block;
                      content: " ";
                      margin-top: -75px;
                      height: 75px;
                      visibility: hidden;
                    }

                        ''')
            }

            body(class:"#f5f5f5 grey lighten-4"){
                script('',type:'text/javascript',src:'https://code.jquery.com/jquery-2.1.1.min.js')
                script('',type:'text/javascript',src:'https://cdnjs.cloudflare.com/ajax/libs/materialize/0.97.0/js/materialize.min.js')
                header(class:"navbar-fixed"){
                    nav(class:"top-nav #80deea cyan lighten-3"){
                        div(class:"container"){
                            div(class:"nav-wrapper"){
                                a(href:"#",class:"brand-logo",c_name)
                                ul(id:"nav-mobile",class:"right hide-on-med-and-down"){
                                    li(p_name)
                                }
                            }
                        }
                        //end of NAV
                    }
                    //end of HEADER
                }


                div(class:"container",style:""){
                    br()
//REPORT SUMMARY
                    h5("Report Summary :")
                    div(class:"row",name:"summary"){
                        h2("  ")
                        div(class:"col s12 m9"){
                            div(class:"card blue-grey lighten-5",style:"padding-left: 25px;margin-left: 22px;"){
                                div(class:"card-content black-text"){
                                    span(class:"card-title black-text",proj_name)
                                    br()
                                    p(style:"text-size:5px;","Report taken at :")
                                    def today = new Date()
                                    p(today)
                                }
                            }
                        }
                    }

// Collapsible menu style - TESTSUITE / TESTCASE index

                    h5("TestSuite/TestCase Index :")
                    br()
                    div(class:"row",name:"index"){
                        ul(class:"collapsible popout"){
                            project.getTestSuiteList().each
                                    {
                                        testSuite->
                                            li{
                                                div(class:"collapsible-header",testSuite.getName()){
                                                    i(class:"material-icons","dashboard"){}
                                                }
                                                div(class:"collapsible-body",style:"display: block;padding-left: 35px;padding-top: 25px;"){
                                                    testSuite.getTestCaseList().each
                                                            {
                                                                testCase->
                                                                    def tcasename="#"+testCase.getName()
                                                                    i(class:"material-icons tiny","done"){}
                                                                    a(href:tcasename,"  "+testCase.getName())
                                                                    br()
                                                                    br()
                                                            }
                                                    //end of collapse body
                                                }
                                            }
                                    }
//end of collapse popup
                        }
//end of TESTSUTE INDEX
                    }
                    br()
                    h5("Test Execution Data :")
                    br()


                    def suite_name=[]
                    def case_name=[]
                    def step_name=[]


                    project.getTestSuiteList().each
                            {
                                testSuite->
                                    div(class:"row"){

                                        div(style:"text-align:center"){i(class:"medium material-icons center","dashboard")}
                                        div(style:"text-align:center"){h4(testSuite.getName())}

                                        testSuite.getTestCaseList().each
                                                {
                                                    testCase->
                                                        div(class:"col s12"){
                                                            h5(sytle:"",id:testCase.getName(),testCase.getName()){i(class:"small material-icons left","done")}
                                                            br()
                                                            testCase.getTestStepsOfType(com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep.class).each
                                                                    {
                                                                        tests->
                                                                            div(class:"card"){
                                                                                div(class:"card-content"){
                                                                                    span(class:"card-tilte",tests.getName().trim()){i(class:"material-icons left","formatlistbulleted")}
                                                                                    def response = tests.httpRequest.response
                                                                                    if( response == null )
                                                                                    {
                                                                                        div(class:"no-run"){p("Not yet executed!!")}
                                                                                        SoapUI.log.info "Missing Response for TestStep : " + tests.testStep.testCase.testSuite.name + "=>"+tests.testStep.testCase.name+ "->"+tests.name
                                                                                        return
                                                                                    }
                                                                                    def data = response.getRawResponseData()
                                                                                    if( data == null || data.length == 0 )
                                                                                    {
                                                                                        div(class:"no-run"){p("Not yet executed!!")}
                                                                                        SoapUI.log.info "Empty Response data for TestStep : "+ tests.testStep.testCase.testSuite.name + "=>"+tests.testStep.testCase.name+ "->"+tests.name
                                                                                        return
                                                                                    }
                                                                                    else
                                                                                    {
                                                                                        def rawRequest = new String(response.getRawRequestData())
                                                                                        def rawResponse = new String(response.getRawResponseData())
                                                                                        def Assertioncounter = tests.getAssertionList().size()

                                                                                        ul(class:"collapsible"){
                                                                                            li{
                                                                                                div(class:"collapsible-header","Request"){
                                                                                                    i(class:"material-icons","input"){}
                                                                                                }
                                                                                                div(class:"collapsible-body",style:"/*display: block;padding-left: 35px;padding-top: 25px;*/"){
                                                                                                    p(style:"white-space: pre-wrap;word-wrap: break-word;",rawRequest)
                                                                                                    br()
                                                                                                    rawRequest=null
                                                                                                }
                                                                                            }
                                                                                            li{
                                                                                                div(class:"collapsible-header","Response"){
                                                                                                    i(class:"material-icons","play_for_work"){}
                                                                                                }
                                                                                                div(class:"collapsible-body",style:"/*display: block;padding-left: 35px;padding-top: 25px;*/"){
                                                                                                    p(style:"white-space: pre-wrap;word-wrap: break-word;",rawResponse)
                                                                                                    br()
                                                                                                    rawResponse=null
                                                                                                }
                                                                                            }
                                                                                            //end of collapse popup
                                                                                        }

//CODE for ASSERTION  TABLE starts here

                                                                                        br()
                                                                                        h6("Assertions:")
                                                                                        br()
                                                                                        table{
                                                                                            thead{
                                                                                                tr{
                                                                                                    th(style:"text-align:center","Status")
                                                                                                    th(style:"text-align:center","Type")
                                                                                                    th("Detail")
                                                                                                }
                                                                                            }
                                                                                            tbody{

                                                                                                for (AssertionCount in 0..Assertioncounter-1)
                                                                                                {
                                                                                                    tr{


                                                                                                        if(tests.getAssertionAt(AssertionCount).getStatus().toString()=="VALID"){
                                                                                                            td(style:"background-color:#acdeac;color:white;text-align:center;",tests.getAssertionAt(AssertionCount).getStatus().toString())
                                                                                                        }
                                                                                                        else if(tests.getAssertionAt(AssertionCount).getStatus().toString()=="FAILED"){
                                                                                                            td(style:"background-color:#ff6666;color:white;text-align:center;",tests.getAssertionAt(AssertionCount).getStatus().toString())
                                                                                                        }
                                                                                                        else{
                                                                                                            td(tests.getAssertionAt(AssertionCount).getStatus().toString())
                                                                                                        }


                                                                                                        td(style:"text-align:center;",tests.getAssertionAt(AssertionCount).ID)
                                                                                                        td(tests.getAssertionAt(AssertionCount).getName().toString())

                                                                                                    }
                                                                                                    //end of FOR LOOP
                                                                                                }

                                                                                            }

                                                                                        }

//CODE end of ASSERTION  TABLE



                                                                                        SoapUI.log.info "done"

                                                                                    }

                                                                                    //end of STEP- CARD CONTENT
                                                                                }
                                                                                //end of STEP- CARD div
                                                                            }

                                                                    }









                                                            testCase.getTestStepsOfType(com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep.class).each
                                                                    {
                                                                        tests->
                                                                            div(class:"card"){
                                                                                div(class:"card-content"){
                                                                                    span(class:"card-tilte",tests.getName().trim()){i(class:"material-icons left","formatlistbulleted")}
                                                                                    def response = tests.httpRequest.response
                                                                                    if( response == null )
                                                                                    {
                                                                                        div(class:"no-run"){p("Not yet executed!!")}
                                                                                        SoapUI.log.info "Missing Response for TestStep : " + tests.testStep.testCase.testSuite.name + "=>"+tests.testStep.testCase.name+ "->"+tests.name
                                                                                        return
                                                                                    }
                                                                                    def data = response.getRawResponseData()
                                                                                    if( data == null || data.length == 0 )
                                                                                    {
                                                                                        div(class:"no-run"){p("Not yet executed!!")}
                                                                                        SoapUI.log.info "Empty Response data for TestStep : "+ tests.testStep.testCase.testSuite.name + "=>"+tests.testStep.testCase.name+ "->"+tests.name
                                                                                        return
                                                                                    }
                                                                                    else
                                                                                    {
                                                                                        def rawRequest = new String(response.getRawRequestData())
                                                                                        def rawResponse = new String(response.getRawResponseData())
                                                                                        def Assertioncounter = tests.getAssertionList().size()

                                                                                        ul(class:"collapsible"){
                                                                                            li{
                                                                                                div(class:"collapsible-header","Request"){
                                                                                                    i(class:"material-icons","input"){}
                                                                                                }
                                                                                                div(class:"collapsible-body",style:"/*display: block;padding-left: 35px;padding-top: 25px;*/"){
                                                                                                    p(style:"white-space: pre-wrap;word-wrap: break-word;",rawRequest)
                                                                                                    br()
                                                                                                    rawRequest=null
                                                                                                }
                                                                                            }
                                                                                            li{
                                                                                                div(class:"collapsible-header","Response"){
                                                                                                    i(class:"material-icons","play_for_work"){}
                                                                                                }
                                                                                                div(class:"collapsible-body",style:"/*display: block;padding-left: 35px;padding-top: 25px;*/"){
                                                                                                    p(style:"white-space: pre-wrap;word-wrap: break-word;",rawResponse)
                                                                                                    br()
                                                                                                    rawResponse=null
                                                                                                }
                                                                                            }
                                                                                            //end of collapse popup
                                                                                        }

//CODE for ASSERTION  TABLE starts here

                                                                                        br()
                                                                                        h6("Assertions:")
                                                                                        br()
                                                                                        table{
                                                                                            thead{
                                                                                                tr{
                                                                                                    th(style:"text-align:center","Status")
                                                                                                    th(style:"text-align:center","Type")
                                                                                                    th("Detail")
                                                                                                }
                                                                                            }
                                                                                            tbody{

                                                                                                for (AssertionCount in 0..Assertioncounter-1)
                                                                                                {
                                                                                                    tr{


                                                                                                        if(tests.getAssertionAt(AssertionCount).getStatus().toString()=="VALID"){
                                                                                                            td(style:"background-color:#acdeac;color:white;text-align:center;",tests.getAssertionAt(AssertionCount).getStatus().toString())
                                                                                                        }
                                                                                                        else if(tests.getAssertionAt(AssertionCount).getStatus().toString()=="FAILED"){
                                                                                                            td(style:"background-color:#e6c1c1;color:white;text-align:center;",tests.getAssertionAt(AssertionCount).getStatus().toString())
                                                                                                        }
                                                                                                        else{
                                                                                                            td(tests.getAssertionAt(AssertionCount).getStatus().toString())
                                                                                                        }


                                                                                                        td(style:"text-align:center;",tests.getAssertionAt(AssertionCount).ID)
                                                                                                        td(tests.getAssertionAt(AssertionCount).getName().toString())

                                                                                                    }
                                                                                                    //end of FOR LOOP
                                                                                                }

                                                                                            }

                                                                                        }

//CODE end of ASSERTION  TABLE



                                                                                        SoapUI.log.info "done"

                                                                                    }

                                                                                    //end of STEP- CARD CONTENT
                                                                                }
                                                                                //end of STEP- CARD div
                                                                            }

                                                                    }





                                                            '''
                                        testCase.getTestStepsOfType(com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep.class).each
                                                {
                                                    tests->
                                                    div(class:"step"){
                                                    h4(tests.getName())
                                                        def response = tests.httpRequest.response
                                                        if( response == null )
                                                        {
                                                            div(class:"no-run"){p("Not yet executed!!")}
                                                            SoapUI.log.info "Missing Response for TestStep : " + tests.testStep.testCase.testSuite.name + "=>"+tests.testStep.testCase.name+ "->"+tests.name
                                                            return
                                                        }
                                                        def data = response.getRawResponseData()
                                                        if( data == null || data.length == 0 )
                                                        {
                                                            div(class:"no-run"){p("Not yet executed!!")}
                                                            SoapUI.log.info "Empty Response data for TestStep : "+ tests.testStep.testCase.testSuite.name + "=>"+tests.testStep.testCase.name+ "->"+tests.name
                                                            return
                                                        }
                                                        else
                                                        {


                                                            //def rawRequest = new String(response.getRawRequestData(),"UTF-8")
                                                            //def rawResponse = new String(response.getRawResponseData(),"UTF-8")
                                                           
                                                            def rawRequest = new String(response.getRawRequestData())
                                                            def rawResponse = new String(response.getRawResponseData()) 

      ul(class:"collapsible"){
            li{
                div(class:"collapsible-header","Request"){
                i(class:"material-icons","input"){}   
                }
                div(class:"collapsible-body",style:"/*display: block;padding-left: 35px;padding-top: 25px;*/"){
                p(style:"white-space: pre-wrap;word-wrap: break-word;",rawRequest)                                      
                br()        
                }
            
            }
             li{
                div(class:"collapsible-header","Response"){
                i(class:"material-icons","play_for_work"){}   
                }
                div(class:"collapsible-body",style:"/*display: block;padding-left: 35px;padding-top: 25px;*/"){
                p(style:"white-space: pre-wrap;word-wrap: break-word;",rawResponse)                                     
                br()        
                }
            }     
      //end of collapse popup
     }

                                                            SoapUI.log.info "done at step level"

                                                        }
                                               //end of STEP div
                                                }

                                                }

                                                '''
                                                            //end of CASE div
                                                        }
                                                }
                                        //end of SUITE div
                                    }
                            }


//end of material - COINTAINER div
                }

//floating icon
                div(class:"fixed-action-btn",style:"top: 20px; right: 24px;width: 300px;height: 315px;padding-left: 200px;")
                        {
                            a(class:"btn-floating btn-large waves-effect waves-light yellow"){
                                i(class:"large material-icons","reorder"){}
                            }

                            ul(style:"top: 65px;right:-4px"){
                                a(class:"btn-floating red",style:"transform: scaleY(0.4) scaleX(0.4) translateZ(140px); opacity: 0;"){
                                    i(class:"material-icons","insert_chart"){}  }
                            }
                            ul(style:"top: 105px; right: -104px;"){
                                a(class:"btn-floating red",style:"transform: scaleY(0.4) scaleX(0.4) translateZ(140px); opacity: 0;"){
                                    i(class:"material-icons","insert_chart"){}  }
                            }
                            ul(style:"top: 97px; right: -237px;"){
                                a(class:"btn-floating red",style:"transform: scaleY(0.4) scaleX(0.4) translateZ(140px); opacity: 0;"){
                                    i(class:"material-icons","insert_chart"){}  }
                            }
//end of Floating menu  
                        }


//end of BODY
            }
//end of HTML
        }


        UISupport.showInfoMessage("Report Baked & ready to be served in your favourite browser :) \n Note: We feel soory if you are an IE guy ;)","Tada !!")

//SoapUI.log.info writer.toString()

        def htmlFile = new File("C:\\Users\\EASWARSX\\Desktop\\material_soapui_test.html")
        htmlFile.write(writer.toString())

        Desktop.getDesktop().browse(htmlFile.toURI());
        //end of PERFORM method
    }

}
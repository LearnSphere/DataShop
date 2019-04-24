/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.logging;

import java.util.Arrays;
import java.util.List;

import edu.cmu.pslc.logging.element.ConditionElement;
import edu.cmu.pslc.logging.element.CustomFieldElement;
import edu.cmu.pslc.logging.element.DatasetElement;
import edu.cmu.pslc.logging.element.InterpretationElement;
import edu.cmu.pslc.logging.element.LevelElement;
import edu.cmu.pslc.logging.element.MetaElement;
import edu.cmu.pslc.logging.element.ProblemElement;
import edu.cmu.pslc.logging.element.PropertyElement;
import edu.cmu.pslc.logging.element.SkillElement;
import edu.cmu.pslc.logging.element.StepElement;
import edu.cmu.pslc.logging.element.StepSequenceElement;

/**
 * This is a sample application which uses the high level
 * API of the Data Shop's tutor logging jar.
 * It is merely an example of how some methods can be called.
 * It produces a file in the current directory.
 *
 * @author Alida Skogsholm
 * @version $Revision: 5610 $
 * <BR>Last modified by: $Author: jrankin $
 * <BR>Last modified on: $Date: 2009-07-27 11:02:03 -0400 (Mon, 27 Jul 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public class Sample {

    /** Default constructor. */
    public Sample() { }

    /**
     * Just do something cool and nothing more.
     */
    public final void justDoIt() {
        FileLogger fileLogger = FileLogger.create("MyPlainFile.xml");
        doSomethingCool(fileLogger);

        OliDiskLogger oliDiskLogger = OliDiskLogger.create("MyOliFile.xml");
        doSomethingCool(oliDiskLogger);

        //OliDatabaseLogger oliDbLogger = OliDatabaseLogger.create(false); // log to QA
        //doSomethingCool(oliDbLogger);
    }

    /**
     * Logs some random actions.
     * @param msgLogger the message logger to use
     */
    private void doSomethingCool(MessageLogger msgLogger) {

        String userId = "joe_cool";
        String sessionId = Message.generateGUID("JUNK");
        String timeString = "2006-08-30 11:22:33";
        String timeZone = "EST";
        String className = "Elementary Logging";
        String school = "CMU";
        String period = "First Period";
        String description = "Online Course in Teaching";
        String instructorOne = "Jim Teacher";
        String instructorTwo = "Mary Instructor";
        String dfa = "The DFA";
        String problemName = "ChemPT1";
        String problemContext = "Chemistry Problem One";

        MetaElement metaElement = new MetaElement(userId, sessionId, timeString, timeZone);

        ContextMessage contextMsg = ContextMessage.createStartProblem(metaElement);
        contextMsg.setClassName(className);
        contextMsg.setSchool(school);
        contextMsg.setPeriod(period);
        contextMsg.setClassDescription(description);
        contextMsg.addInstructor(instructorOne);
        contextMsg.addInstructor(instructorTwo);
        contextMsg.addDfa(dfa);
        contextMsg.addSkill(new SkillElement("1.0", "LoggingSkill", "General", "Basic"));
        ProblemElement problem = new ProblemElement(problemName, problemContext);
        LevelElement sectionLevel = new LevelElement("Section", "One", problem);
        LevelElement unitLevel = new LevelElement("Unit", "A", sectionLevel);
        contextMsg.setDataset(new DatasetElement("MyLoggingDataset", unitLevel));
        contextMsg.addCondition(new ConditionElement("WorkedExamples", "experimental"));
        contextMsg.addCondition(new ConditionElement("control"));
        contextMsg.addCustomField(new CustomFieldElement("Blah", "blah"));

        msgLogger.log(contextMsg);

        ToolMessage toolMsg = ToolMessage.create(contextMsg);
        toolMsg.setAsAttempt();
        toolMsg.addSai("ButtonOne", "PressButton", "box");
        toolMsg.addCustomField(new CustomFieldElement("Equation", "y=x+ab"));
        toolMsg.addCustomField(new CustomFieldElement("Whatever", "zero"));

        msgLogger.log(toolMsg);

        TutorMessage tutorMsg = TutorMessage.create(toolMsg);
        tutorMsg.setAsCorrectAttemptResponse();
        tutorMsg.addSai("ButtonOne", "PressButton", "square");
        tutorMsg.addSkill(new SkillElement("Dictation", "General", "Basic"));
        tutorMsg.addCustomField(new CustomFieldElement("Equation", "y=x+ab+1"));
        tutorMsg.addCustomField(new CustomFieldElement("Whatever", "one"));
        //of course, you wouldn't really have an interpretation with an SAI (event descriptor)
        //this is just an example
        StepSequenceElement corSeq = StepSequenceElement.createCorrectSequence();
        corSeq.setOrderedFlag(Boolean.TRUE);
        corSeq.addStep(new StepElement("MyCorrectStep"));
        StepSequenceElement incSeq = StepSequenceElement.createIncorrectSequence();
        incSeq.setOrderedFlag(Boolean.FALSE);
        incSeq.addStep(new StepElement("BadStepOne"));
        incSeq.addStep(new StepElement("BadStepTwo"));
        InterpretationElement interp = new InterpretationElement(Boolean.TRUE, corSeq, incSeq);
        tutorMsg.addInterpretation(interp);

        msgLogger.log(tutorMsg);

        PlainMessage plainMsg = PlainMessage.create(contextMsg);
        plainMsg.addProperty(new PropertyElement("plain property"));
        plainMsg.addProperty(new PropertyElement("name", "contents"));
        List<String> entryList = Arrays.asList("one", "two", "three");
        plainMsg.addProperty(new PropertyElement("entry list name", entryList));

        msgLogger.log(plainMsg);

        msgLogger.close();

    } // end doSomethingCool method

    /**
     * Main.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // create the sample
        Sample sample = new Sample();

        // read file, parse it and do something cool with it
        sample.justDoIt();
    } // end main

} // end Sample class

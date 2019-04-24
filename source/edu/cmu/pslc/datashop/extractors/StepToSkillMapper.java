package edu.cmu.pslc.datashop.extractors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.SkillDao;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Tool to map steps to skills.
 * Note that the file must be of a given format with a strict data set level hierarchy
 * akin to "dataset - unit - section - problem" as in the past.
 *
 * @author Alida Skogsholm
 * @version $Revision: 7807 $<BR>
 *          Last modified by: $Author: ctipper $<BR>
 *          Last modified on: $Date: 2012-08-03 14:11:16 -0400 (Fri, 03 Aug 2012) $
 * <!-- $KeyWordsOff: $-->
 */
public class StepToSkillMapper {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Name of the file that contains the mapping */
    private String fileName;

    /** the name of the skillModel being inserted */
    private String skillModelName;

    /** Default constructor. */
    public StepToSkillMapper() {
    }

    /**
     * Constructor.
     * @param fileName String of the file name
     */
    public StepToSkillMapper(String fileName) {
        this.fileName = fileName;
    }

    /**Getter for filename.
     * @return String fileName*/
    public String getFileName() {
        return fileName;
    }

    /**Setter for filename.
     * @param fileName String filename*/
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**Setter for skillModelName.
     * @param skillModelName String skillModelName*/
    public void setSkillModelName(String skillModelName) {
        this.skillModelName = skillModelName;
    }

    /**Getter for skillModelName.
     * @return String skillModelName*/
    public String getSkillModelName() {
        return skillModelName;
    }

    /**
     * Read file and add the skills to the skill table (if necessary), and
     * associate the steps to the skills.
     */
    public void addSkills() {
        int correctLines = 0;
        int importedLines = 0;
        int errorLines = 0;
        try {
            File inputFile = new File(fileName);
            FileReader fileReader = new FileReader(inputFile);
            BufferedReader bufReader = new BufferedReader(fileReader);
            String problemName;
            String selection;
            String action;
            String input = "";
            String skillName;
            String catName;
            String unitName;
            String sectionName;
            String curriculumName;
            boolean firstPass = true;
            boolean noErrors = true;

            String line = bufReader.readLine();
            while (line != null && noErrors) {
                importedLines++;

                problemName = null;
                selection = null;
                action = null;
                input = "";
                skillName = null;
                catName = null;
                unitName = null;
                sectionName = null;
                curriculumName = null;

                String delim = "\t";

                try {
                    StringTokenizer st = new StringTokenizer(line);

                    curriculumName = st.nextToken(delim);
                    logger.debug("curriculumName = " + curriculumName);
                    unitName = st.nextToken(delim);
                    logger.debug("unitName = " + unitName);
                    sectionName = st.nextToken(delim);
                    logger.debug("sectionName = " + sectionName);
                    problemName = st.nextToken(delim);
                    logger.debug("problemName = " + problemName);
                    selection = st.nextToken(delim);
                    logger.debug("selection = " + selection);
                    action = st.nextToken(delim);
                    action = action.trim();
                    logger.debug("action [" + action + "]");

                    //this will put the input to blank depending on whether we
                    //want to use the input as a part of the step identification.
                    if (UtilConstants.USE_INPUT_IN_STEP_IDENTIFICATION) {
                        input = st.nextToken(delim);
                        logger.debug("input = " + input);
                    }

                    skillName = st.nextToken(delim);
                    logger.debug("skillName = " + skillName);

                    if (st.hasMoreElements()) {
                        catName = st.nextToken(delim);
                        logger.debug("skillCatagoryName = " + catName);
                    } else {
                        catName = null;
                    }

                    DatasetItem datasetItem = getDataset(curriculumName);
                    logger.info("Dataset is " + datasetItem + " for " + curriculumName);
                    if (datasetItem != null) {
                        SkillModelItem skillModelItem = getSkillModel(datasetItem, skillModelName);

                        SubgoalItem subgoalItem = getSubgoalItem(datasetItem,
                                unitName, sectionName, problemName, selection,
                                action, input);

                        if (firstPass) {
                            if (skillModelItem == null) {
                                noErrors = false;
                                errorLines++;
                                break;
                            } else {
                                firstPass = false;
                            }
                        }

                        if (subgoalItem != null) {
                            String info = "Mapping Subgoal: "
                                    + subgoalItem.toString() + " -- To skillName: "
                                    + skillName;
                            if (catName != null) {
                                info += " ( " + catName + " )";
                            }

                            SkillItem newSkill = mapSkill(subgoalItem, skillName,
                                    catName, skillModelItem);
                            if (newSkill != null) {
                                logger.info(info + "... Success!");
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Skill " + newSkill.getSkillName()
                                            + " has " + newSkill.getSubgoalsExternal().size()
                                            + " subgoals");
                                }
                                correctLines++;
                            } else {
                                logger.info(info + "... Error!");
                                errorLines++;
                            }

                            skillModelItem.addSkill(newSkill);

                        } else {
                            logger.warn("Warning: No subgoal found from given information on line "
                                    + importedLines + ".  Skipping line and continuing.");
                            logger.warn("Line Number " + importedLines + " : " + line.toString());
                            errorLines++;
                        }
                        //datasetItem.resetModifiedTime();
                        //TODO Log modified time.
                        DaoFactory.DEFAULT.getDatasetDao().saveOrUpdate(datasetItem);
                    } else {
                        logger.info("Dataset not found: " + curriculumName);
                    }

                } catch (NoSuchElementException exception) {
                    logger.error("Malformed Line Number: "
                                    + importedLines
                                    + " .  Incorrect number of elements in the line. Skipping.");
                    logger.debug("Line: " + line.toString());
                    logger.debug(exception.getMessage(), exception);
                    errorLines++;
                }
                line = bufReader.readLine();
            } // end while more lines to read

            bufReader.close();
        } catch (FileNotFoundException exception) {
            logger.warn(exception.getMessage(), exception);
        } catch (IOException exception) {
            logger.warn(exception.getMessage(), exception);
        } finally {
            logger.info("Imported Lines: " + importedLines
                    + "; Subgoals Mapped: " + correctLines
                    + "; Errors: " + errorLines);
        }

    } // end addSkills method

    /**
     * Gets the dataset item for the given name.
     * Will create one if it doesn't exist.
     * @param datasetName name of the dataset
     * @return a dataset item
     */
    private DatasetItem getDataset(String datasetName) {
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        DatasetItem datasetItem = null;
        Collection datasetList = datasetDao.find(datasetName);
        boolean found = false;
        for (Iterator dsIter = datasetList.iterator(); dsIter.hasNext();) {
            datasetItem = (DatasetItem)dsIter.next();
            if (datasetItem.getDatasetName().equals(datasetName)) {
                found = true;
                break;
            }
        }
        if (found) {
            //update the dataset to let other programs know we have modified it.
            //datasetItem.setModifiedTime(new Date());
            //TODO log modified to system logging
            datasetDao.saveOrUpdate(datasetItem);
            datasetItem = datasetDao.get((Integer)datasetItem.getId());
            return datasetItem;
        }

        return null;
    }

    /**
     * Gets the skillModelItem given the skill model name.
     * Will return null if the skill model already exists in the dataset.
     * @param datasetItem the dataset item
     * @param skillModelName the name of the skill model
     * @return SkillModelItem - the skill model that is being mapped. Null if error.
     */
    private SkillModelItem getSkillModel(DatasetItem datasetItem, String skillModelName) {
        SkillModelDao skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();
        SkillModelItem skillModelItem = null;

        datasetItem = DaoFactory.DEFAULT.getDatasetDao().get((Integer)datasetItem.getId());
        Collection skillModelList = datasetItem.getSkillModelsExternal();

        boolean found = false;
        for (Iterator smIter = skillModelList.iterator(); smIter.hasNext();) {
            skillModelItem = (SkillModelItem)smIter.next();
            if (skillModelItem.getSkillModelName().equals(skillModelName.trim())) {
                found = true;
                break;
            }
        }

        if (!found) {
            skillModelItem = new SkillModelItem();
            skillModelItem.setDataset(datasetItem);
            skillModelItem.setSkillModelName(skillModelName.trim());
            skillModelItem.setGlobalFlag(Boolean.TRUE); // as there is no owner
            skillModelDao.saveOrUpdate(skillModelItem);
            //notify dataset for the skill model
            datasetItem.addSkillModel(skillModelItem);
        }

        return skillModelItem;
    }

    /**
     * Returns a data set level item.
     * @param list list of dataset levels
     * @param title the title of the dataset level
     * @param name the name of the dataset level
     * @return a dataset level item object
     */
    private DatasetLevelItem getDatasetLevel(Collection list, String title, String name) {
        if (logger.isDebugEnabled()) {
            logger.debug("Search for Title: " + title + " Name: " + name);
        }
        DatasetLevelItem newItem = null;
        boolean found = false;
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            newItem = (DatasetLevelItem)iter.next();
            if (newItem.getLevelName().equals(name) && newItem.getLevelTitle().equals(title)) {
                found = true;
                break;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Found      Title: " + newItem.getLevelTitle()
                        + " Name: " + newItem.getLevelName());
            }
        }

        if (found) {
            return newItem;
        }

        return null;
    }

    /**
     * Returns a data set level item.
     * @param levelItem the dataset level item
     * @param name the name of the dataset level
     * @return a dataset level item object, or null if not found
     */
    private ProblemItem getProblem(DatasetLevelItem levelItem, String name) {
        ProblemItem problemItem = null;

        Collection list = levelItem.getProblemsExternal();

        boolean found = false;
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            problemItem = (ProblemItem)iter.next();
            if (problemItem.getProblemName().equals(name)) {
                found = true;
                break;
            }
        }

        if (found) {
            return problemItem;
        }

        return null;
    }

    /**
     * Returns a subgoal if it exists.
     * @param problemItem the problem item
     * @param selection the selection
     * @param action the action
     * @param input the input
     * @return a subgoal item if it exists, null otherwise
     */
    private SubgoalItem getItemByProblemAndSAI(ProblemItem problemItem,
            String selection, String action, String input) {
        SubgoalItem subgoalItem = null;
        String name = selection;
        if (action != null && action.length() > 0) {
            name += " " + action;
        }

        Collection list = problemItem.getSubgoalsExternal();
        if (logger.isDebugEnabled()) {
            logger.debug("Problem " + problemItem.getProblemName()
                    + " has " + list.size() + " subgoals");
        }

        boolean found = false;
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            subgoalItem = (SubgoalItem)iter.next();
            if (subgoalItem.getSubgoalName().equals(name)) {
                found = true;
                break;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Subgoal name is [" + subgoalItem.getSubgoalName()
                        + "], looking for [" + name + "]");
            }
        }

        if (found) {
            return subgoalItem;
        }

        return null;
    }

    /**
     * Gets the subgoal Id from the curriculum, unit, section, problem and then SAI.
     * @param datasetItem the dataset item
     * @param unitName the name of the unit as a string
     * @param sectionName the name of the section as a string
     * @param problemName the name of the problem as a string
     * @param selection the selection
     * @param action the action
     * @param input the input
     * @return subgoalItem that is the subgoal Id or null if none found.
     */
    private SubgoalItem getSubgoalItem(DatasetItem datasetItem, String unitName,
            String sectionName, String problemName,
            String selection, String action, String input) {
        SubgoalItem subgoalItem = null;
        String datasetName = datasetItem.getDatasetName();

        logger.debug("Looking for subgoal:: dataset: " + datasetName
                + " unit: " + unitName
                + " section: " + sectionName
                + " problem: " + problemName
                + " SAI:" + selection + "|" + action + "|" + input);

        String unitTitle = DatasetLevelItem.UNIT_TITLE;
        //Geometry-AllStudents Hack Hack Hack
        if (unitName.equals("Default") && sectionName.equals("Default")) {
            unitTitle = "Default";

            DatasetLevelItem unitItem =
                getDatasetLevel(datasetItem.getDatasetLevelsExternal(),
                        unitTitle, unitName);
            if (unitItem != null) {
                ProblemItem problemItem = getProblem(unitItem, problemName);
                if (problemItem != null) {
                    subgoalItem = getItemByProblemAndSAI(problemItem, selection, action, input);
                    if (subgoalItem == null) {
                        logger.info("Subgoal(" + selection + " " + action + ") not found");
                    }
                } else {
                    logger.info("Problem(" + problemName + ") not found");
                }
            } else {
                logger.info("Unit(" + unitName + ") not found");
            }
        } else {
            DatasetLevelItem unitItem =
                getDatasetLevel(datasetItem.getDatasetLevelsExternal(),
                        unitTitle, unitName);

            if (unitItem != null) {
                DatasetLevelItem sectionItem =
                    getDatasetLevel(unitItem.getChildrenExternal(),
                            DatasetLevelItem.SECTION_TITLE, sectionName);
                if (sectionItem != null) {
                    ProblemItem problemItem = getProblem(sectionItem, problemName);
                    if (problemItem != null) {
                        subgoalItem = getItemByProblemAndSAI(problemItem, selection, action, input);
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Problem(" + problemName + ") not found");
                        }
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Section(" + sectionName + ") not found");
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Unit(" + unitName + ") not found");
                }
            }
        }

        return subgoalItem;
    }

    /**
     * Checks to see that the problem name is unique and that the file can be
     * found.
     * @param subgoalItem the subgoal Item that we will be mapping the skill to.
     * @param skillName the name of the skill.
     * @param skillCategory the category of the skill.
     * @param skillModelItem the skill model being mapped to.
     * @return boolean of success.  Will return true if mapping was successful, false if not.
     */
    private SkillItem mapSkill(SubgoalItem subgoalItem, String skillName, String skillCategory,
            SkillModelItem skillModelItem) {

        skillModelItem = DaoFactory.DEFAULT.getSkillModelDao().get((Long)skillModelItem.getId());

        SkillDao skillDao = DaoFactory.DEFAULT.getSkillDao();
        SkillItem skillItem = null;

        if (subgoalItem == null) {
            logger.error("mapSkill: subgoal is null");
            return null;
        }

        List list = skillModelItem.getSkillsExternal();

        boolean found = false;
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            skillItem = (SkillItem)iter.next();
            if (logger.isDebugEnabled()) {
                logger.debug("Skill item is " + skillItem);
            }
            if (skillItem.getSkillName().equals(skillName)) {
                if (skillCategory == null) {
                    if (skillItem.getCategory() == null) {
                        found = true;
                        break;
                    }
                } else {
                    if (skillCategory != null) {
                        if (skillItem.getCategory().equals(skillCategory)) {
                            found = true;
                            break;
                        }
                    }
                }
            }
        } // end for loop

        if (!found) {
            skillItem = new SkillItem();
            skillItem.setSkillName(skillName);
            skillItem.setCategory(skillCategory);
            skillItem.setSkillModel(skillModelItem);
            //add subgoal to populate the map table
            skillItem.addSubgoal(subgoalItem);
            skillDao.saveOrUpdate(skillItem);
            if (logger.isDebugEnabled()) {
                logger.debug("Skill created: " + skillName + ", " + skillCategory + ", "
                        + skillModelItem);
            }
        } else {
            skillItem.addSubgoal(subgoalItem);
            skillDao.saveOrUpdate(skillItem);
            if (logger.isDebugEnabled()) {
                logger.debug("Skill   found: " + skillName + ", " + skillCategory + ", "
                        + skillModelItem);
            }
        }

        return skillItem;

    } // mapSkill

    /**
     * Parse the command line arguments to get the file and curriculum names.
     * @param args Command line arguments
     */
    protected void handleOptions(String[] args) {
        if (args == null || args.length == 0) {
            return;
        }

        java.util.ArrayList argsList = new java.util.ArrayList();
        for (int i = 0; i < args.length; i++) {
            argsList.add(args[i]);
        }

        // loop through the arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h")) {
                displayUsage();
                System.exit(0);
            } else if (args[i].equals("-f")) {
                if (++i < args.length) {
                    setFileName(args[i]);
                } else {
                    System.err.println("Error: a file name must be specified with: -f");
                    displayUsage();
                    System.exit(1);
                }
            } else if (args[i].equals("-m")) {
                if (++i < args.length) {
                    this.setSkillModelName(args[i]);
                } else {
                    System.err.println("Error: a skill model name must be specified with: -m");
                    displayUsage();
                    System.exit(1);
                }
            } else {
                System.err.println("Error: improper command line arguments: "
                        + argsList);
                displayUsage();
                System.exit(1);
            } // end if then else
        } // end for loop
    } // end handleOptions

    /**
     * Displays the command line arguments for this program.
     */
    protected void displayUsage() {
        System.err.println("\nUSAGE: java -classpath ... "
                + "StepToSkillMapper [-f file_name] [-m skill_model_name]");
        System.err.println("Option descriptions:");
        System.err.println("\t-h\t usage info");
        System.err.println("\t-f\t input file name");
        System.err.println("\t-m\t skill model name");
    }

    /**
     * Main program to run.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("StepToSkillMapper.main");
        try {
            StepToSkillMapper mapper = ExtractorFactory.DEFAULT.getStepToSkillMapper();

            // parse arguments to get file name and curriculum name
            mapper.handleOptions(args);

            logger.info("StepToSkillMapper starting on model "
                    + mapper.getSkillModelName() + " and file " + mapper.getFileName());

            // read file,
            // create skill model (if necessary),
            // add the skills to the skill table (if necessary),
            // and associate the steps to the skills
            mapper.addSkills();

        } catch (Throwable throwable) {
            logger.error("Unknown error in main method.", throwable);
        } finally {
            logger.info("StepToSkillMapper done.");
        }
    } // end main

} // end StepToSkillMapper class

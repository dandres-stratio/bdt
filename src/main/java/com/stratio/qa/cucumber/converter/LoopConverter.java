/*
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.qa.cucumber.converter;

import com.stratio.qa.utils.ThreadProperty;
import gherkin.events.PickleEvent;
import gherkin.pickles.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoopConverter {
    public static List<PickleEvent> transformPickleEventWithLoopTags(PickleEvent pe) {
        List<PickleEvent> pickleEventList = new ArrayList<>();
        for (PickleTag pickleTag : pe.pickle.getTags()) {
            // Loop
            if (pickleTag.getName().toUpperCase().matches("\\s*@LOOP.*")) {
                transformLoop(pe, pickleTag, pickleEventList);
                break;
            }
            // ProgLoop
            if (pickleTag.getName().toUpperCase().matches("\\s*@PROGLOOP.*")) {
                transformProgLoop(pe, pickleTag, pickleEventList);
                break;
            }
            // MultiLoop
            if (pickleTag.getName().toUpperCase().matches("\\s*@MULTILOOP.*")) {
                transformMultiLoop(pe, pickleTag, pickleEventList);
                break;
            }
        }
        if (pickleEventList.isEmpty()) {
            pickleEventList.add(pe);
        }
        return pickleEventList;
    }

    private static void transformLoop(PickleEvent pe, PickleTag pickleTag, List<PickleEvent> pickleEventList) {
        String listParams = pickleTag.getName().substring((pickleTag.getName().lastIndexOf("(") + 1), (pickleTag.getName().length()) - 1).split(",")[0];
        try {
            String value = System.getProperty(listParams, ThreadProperty.get(listParams));
            if (value == null) {
                throw new Exception("@errorMessage(Variable__" + listParams + "__is__not__defined.)");
            }
            String[] elems = value.split(",");
            String paramReplace = pickleTag.getName().substring((pickleTag.getName().lastIndexOf("(") + 1), (pickleTag.getName().length()) - 1).split(",")[1];
            int numElem = 0;
            for (String elem : elems) {
                pickleEventList.add(generatePickleEvent(elem, numElem, paramReplace, pe.uri, pe.pickle.getName(), pe.pickle.getLanguage(), pe.pickle.getSteps(), pe.pickle.getTags(), pe.pickle.getLocations()));
                numElem++;
            }
        } catch (Exception e) {
            String message = e.getMessage();
            if (!message.contains("@errorMessage")) {
                message = "@errorMessage(" + message.replaceAll(" ", "__") + ".)";
            }
            List<PickleTag> pickleTagList = pe.pickle.getTags();
            pickleTagList.add(new PickleTag(pe.pickle.getLocations().get(0), "@error"));
            pickleTagList.add(new PickleTag(pe.pickle.getLocations().get(0), message));
            Pickle pickle = new Pickle(pe.pickle.getName(), pe.pickle.getLanguage(), pe.pickle.getSteps(), pickleTagList, pe.pickle.getLocations());
            pickleEventList.add(new PickleEvent(pe.uri, pickle));
        }
    }

    private static void transformProgLoop(PickleEvent pe, PickleTag pickleTag, List<PickleEvent> pickleEventList) {
        String listParams = pickleTag.getName().substring((pickleTag.getName().lastIndexOf("(") + 1), (pickleTag.getName().length()) - 1).split(",")[0];
        try {
            String elem = System.getProperty(listParams, ThreadProperty.get(listParams));
            if (elem == null) {
                throw new Exception("@errorMessage(Variable__" + listParams + "__is__not__defined.)");
            }
            int times = Integer.parseInt(elem);
            if (times < 1) {
                throw new Exception("@errorMessage(Variable__" + listParams + "__must__be__higher__than__0.)");
            } else {
                String[] elems = new String[times];
                for (Integer i = 1; i <= times; i++) {
                    elems[i - 1] = i.toString();
                }
                String paramReplace = pickleTag.getName().substring((pickleTag.getName().lastIndexOf("(") + 1), (pickleTag.getName().length()) - 1).split(",")[1];
                int numElem = 0;
                for (String elemAux : elems) {
                    pickleEventList.add(generatePickleEvent(elemAux, numElem, paramReplace, pe.uri, pe.pickle.getName(), pe.pickle.getLanguage(), pe.pickle.getSteps(), pe.pickle.getTags(), pe.pickle.getLocations()));
                    numElem++;
                }
            }
        } catch (Exception e) {
            String message = e.getMessage();
            if (e instanceof NumberFormatException) {
                message = "@errorMessage(Variable__" + listParams + "__is__not__an__integer.)";
            }
            if (!message.contains("@errorMessage")) {
                message = "@errorMessage(" + message.replaceAll(" ", "__") + ".)";
            }
            List<PickleTag> pickleTagList = pe.pickle.getTags();
            pickleTagList.add(new PickleTag(pe.pickle.getLocations().get(0), "@error"));
            pickleTagList.add(new PickleTag(pe.pickle.getLocations().get(0), message));
            Pickle pickle = new Pickle(pe.pickle.getName(), pe.pickle.getLanguage(), pe.pickle.getSteps(), pickleTagList, pe.pickle.getLocations());
            pickleEventList.add(new PickleEvent(pe.uri, pickle));
        }
    }

    private static void transformMultiLoop(PickleEvent pe, PickleTag pickleTag, List<PickleEvent> pickleEventList) {
        Map<String, String[]> params = new HashMap<>();
        String[] elements = pickleTag.getName().substring((pickleTag.getName().lastIndexOf("(") + 1), (pickleTag.getName().length()) - 1).split(",");
        try {
            for (String element : elements) {
                String[] elementParts = element.split("=>");
                String listParam = elementParts[0];
                String paramName = elementParts[1];
                String[] elems;

                String value = System.getProperty(listParam, ThreadProperty.get(listParam));
                if (value == null) {
                    throw new Exception("@errorMessage(Variable__" + listParam + "__is__not__defined.)");
                }

                elems = value.split(",");
                params.put(paramName, elems);
            }
            String[] keys = params.keySet().toArray(new String[params.keySet().size()]);
            int numScenarios = 1;
            for (String key : keys) {
                numScenarios = numScenarios * params.get(key).length;
            }
            List<String[]> replacedValues = new ArrayList<>();
            for (int i = 0; i < numScenarios; i++) {
                replacedValues.add(new String[keys.length]);
            }

            int numRepetitions = 0;
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                String[] keyValues = params.get(key);
                int j = 0;
                int index = 0;
                while (j < numScenarios) {
                    int totalAdded = 0;
                    do {
                        String[] replacedValuesAux = replacedValues.get(j);
                        replacedValuesAux[i] = keyValues[index % keyValues.length];
                        replacedValues.set(j, replacedValuesAux);
                        j++;
                        totalAdded++;
                    }
                    while (totalAdded < numRepetitions);
                    index++;
                }
                if (numRepetitions == 0) {
                    numRepetitions = keyValues.length;
                } else {
                    numRepetitions = numRepetitions * keyValues.length;
                }
            }
            for (String[] values : replacedValues) {
                pickleEventList.add(generatePickleEvent(keys, values, pe.uri, pe.pickle.getName(), pe.pickle.getLanguage(), pe.pickle.getSteps(), pe.pickle.getTags(), pe.pickle.getLocations()));
            }
        } catch (Exception e) {
            String message = e.getMessage();
            if (!message.contains("@errorMessage")) {
                message = "@errorMessage(" + message.replaceAll(" ", "__") + ".)";
            }
            List<PickleTag> pickleTagList = pe.pickle.getTags();
            pickleTagList.add(new PickleTag(pe.pickle.getLocations().get(0), "@error"));
            pickleTagList.add(new PickleTag(pe.pickle.getLocations().get(0), message));
            Pickle pickle = new Pickle(pe.pickle.getName(), pe.pickle.getLanguage(), pe.pickle.getSteps(), pickleTagList, pe.pickle.getLocations());
            pickleEventList.add(new PickleEvent(pe.uri, pickle));
        }
    }

    private static PickleEvent generatePickleEvent(String elem, int numElem, String paramReplace, String uri, String name, String language, List<PickleStep> steps, List<PickleTag> tags, List<PickleLocation> locations) {
        List<PickleStep> newSteps = new ArrayList<>();
        for (PickleStep step : steps) {
            String newStepText = step.getText().replaceAll("<" + paramReplace + ">", elem).replaceAll("<" + paramReplace + ".id>", String.valueOf(numElem));
            List<Argument> argumentListReplaced = replaceArguments("<" + paramReplace + ">", elem, step.getArgument());
            argumentListReplaced = replaceArguments("<" + paramReplace + ".id>", elem, argumentListReplaced);
            newSteps.add(new PickleStep(newStepText, argumentListReplaced, step.getLocations()));
        }
        String newScenarioname = name.replaceAll("<" + paramReplace + ">", elem).replaceAll("<" + paramReplace + ".id>", String.valueOf(numElem));
        Pickle pickle = new Pickle(newScenarioname, language, newSteps, tags, locations);
        return new PickleEvent(uri, pickle);
    }

    private static PickleEvent generatePickleEvent(String[] keys, String[] values, String uri, String name, String language, List<PickleStep> steps, List<PickleTag> tags, List<PickleLocation> locations) {
        List<PickleStep> newSteps = new ArrayList<>();
        for (PickleStep step : steps) {
            String newStepText = step.getText();
            List<Argument> argumentListReplaced = step.getArgument();
            for (int i = 0; i < keys.length; i++) {
                newStepText = newStepText.replaceAll("<" + keys[i] + ">", values[i]);
                argumentListReplaced = replaceArguments("<" + keys[i] + ">", values[i], argumentListReplaced);
            }
            newSteps.add(new PickleStep(newStepText, argumentListReplaced, step.getLocations()));
        }
        String newScenarioname = name;
        for (int i = 0; i < keys.length; i++) {
            newScenarioname = newScenarioname.replaceAll("<" + keys[i] + ">", values[i]);
        }
        Pickle pickle = new Pickle(newScenarioname, language, newSteps, tags, locations);
        return new PickleEvent(uri, pickle);
    }

    private static List<Argument> replaceArguments(String textToReplace, String textReplaced, List<Argument> argumentsList) {
        List<Argument> argumentListReplaced = new ArrayList<>();
        for (Argument argument : argumentsList) {
            if (argument instanceof PickleTable) {
                PickleTable pickleTable = (PickleTable) argument;
                List<PickleRow> pickleRowList = new ArrayList<>(pickleTable.getRows());
                for (int r = 0; r < pickleRowList.size(); r++) {
                    PickleRow pickleRow = pickleRowList.get(r);
                    List<PickleCell> pickleCellList = new ArrayList<>(pickleRow.getCells());
                    for (int c = 0; c < pickleCellList.size(); c++) {
                        PickleCell pickleCell = pickleCellList.get(c);
                        pickleCellList.set(c, new PickleCell(pickleCell.getLocation(), pickleCell.getValue().replaceAll(textToReplace, textReplaced)));
                    }
                    pickleRowList.set(r, new PickleRow(pickleCellList));
                }
                pickleTable = new PickleTable(pickleRowList);
                argumentListReplaced.add(pickleTable);
            } else {
                argumentListReplaced.add(argument);
            }
        }
        return argumentListReplaced;
    }
}

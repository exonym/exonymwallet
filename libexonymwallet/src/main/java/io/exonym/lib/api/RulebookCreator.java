package io.exonym.lib.api;

import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.helpers.NamespaceMngt;
import io.exonym.lib.pojo.*;
import io.exonym.lib.standard.CryptoUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class RulebookCreator {

    public static final String PRODUCTION_FLAG = "production=";
    private Rulebook rulebook = new Rulebook();
    private ArrayList<String> valid = new ArrayList();

    private String rulebookFile = null;

    public RulebookCreator(String name, String path) throws Exception {
        valid.add(Rulebook.MODIFIER_FINAL);
        valid.add(Rulebook.MODIFIER_PROTECTED);
        valid.add(Rulebook.MODIFIER_PUBLIC);

        Path folder = Path.of(path);
        Path rulesPath = folder.resolve(name + ".rulebook");
        Path descriptionPath = folder.resolve(name + ".description");
        Path outPath = folder.resolve(name + "-rulebook.json");
        boolean rv = Files.isRegularFile(rulesPath);
        boolean dv = Files.isRegularFile(descriptionPath);
        if (rv && dv){
            convert(outPath, rulesPath, descriptionPath);

        } else {
            throw new UxException("Ensure you have named the files <name>.rulebook and <name>.description");

        }
    }

    public RulebookCreator(Rulebook rulebook) throws Exception {
        this.rulebook = rulebook;

    }


    public void writeFileToOut(Path out) throws UxException {
        try (BufferedWriter writer = Files.newBufferedWriter(out)) {
            writer.write(this.rulebookFile);
            writer.flush();

        } catch (Exception e) {
            throw new UxException(ErrorMessages.WRITE_FILE_ERROR, out.toAbsolutePath().toString());

        }
    }

    private void convert(Path out, Path rules, Path description) throws Exception {
        buildRulebookRules(rules);
        buildRulebookDescription(description);
        String rulebookId = computeRulebookHash(this.rulebook);

        rulebook.setRulebookId(Namespace.URN_PREFIX_COLON +
                this.rulebook.getDescription().getName().toLowerCase() +
                ":" +
                rulebookId);

        this.rulebookFile = JaxbHelper.gson.toJson(rulebook, Rulebook.class);

        writeFileToOut(out);

    }

    public static String computeRulebookHash(Rulebook rulebook) {
        StringBuilder builder = new StringBuilder();
        RulebookDescription d = rulebook.getDescription();
        builder.append(d.isProduction());
        builder.append(d.getName());
        builder.append(d.getSimpleDescriptionEN());

        for (RulebookItem item : rulebook.getRules()){
            builder.append(item.getId());

        }
        String toHash = builder.toString();
        return CryptoUtils.computeSha256HashAsHex(
                toHash.getBytes(StandardCharsets.UTF_8));


    }

    private void buildRulebookDescription(Path description) throws UxException {
        int index = 0;

        RulebookDescription desc = new RulebookDescription();
        try (BufferedReader reader = Files.newBufferedReader(description)){
            String line = null;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine())!=null){
                if (index==0){
                    desc.setProduction(defineProduction(line));

                } else if (index==1){
                    desc.setName(line);

                } else {
                    builder.append(line);

                }
                index++;

            }
            desc.setSimpleDescriptionEN(builder.toString());
            this.rulebook.setDescription(desc);

        } catch (UxException e){
            throw e;

        } catch (Exception e){
            throw new UxException(ErrorMessages.SERVER_SIDE_PROGRAMMING_ERROR, e);

        }
    }

    private boolean defineProduction(String line) throws UxException {
        if (line!=null && line.startsWith(PRODUCTION_FLAG)){
            try {
                String[] parts = line.split("=");
                return Boolean.parseBoolean(parts[1]);

            } catch (Exception e) {
                throw new UxException(ErrorMessages.INCORRECT_PARAMETERS, e,
                        "Descriptions must start with production=[true|false]");

            }
        } else {
            throw new UxException(ErrorMessages.INCORRECT_PARAMETERS,
                    "Descriptions must start with a production flag.");

        }
    }

    private void buildRulebookRules(Path rules) throws UxException {
        int index = 0;
        try (BufferedReader reader = Files.newBufferedReader(rules)){
            String rule = null;
            while ((rule = reader.readLine())!=null){
                if (rule!=null && rule.length()>0) {
                    RulebookItem item = createRule(rule, index);
                    rulebook.getRules().add(item);
                    index++;

                }
            }
        } catch (UxException e){
            throw e;

        } catch (Exception e){
            throw new UxException(ErrorMessages.SERVER_SIDE_PROGRAMMING_ERROR, e);

        }
    }

    private RulebookItem createRule(String rule, int index) throws UxException {
        if (rule!=null && rule.length()  > 0 ){
            RulebookItem item = new RulebookItem();
            String[] parts = rule.split("#");
            if (parts.length!=2){
                throw new UxException("Rules are defined in the format: 'designation#description'", rule);

            }
            String modifier = parts[0].toLowerCase();
            if (!valid.contains(modifier)){
                throw new UxException("No such modifier " + modifier + " Please use ['public','protected', or 'final']");

            }
            addInterpretations(item, parts[1]);
            item.setDescription(parts[1]);
            String id = Namespace.URN_RULE_PREFIX +
                    index + ":" +
                    modifier + ":" +
                    CryptoUtils.computeSha256HashAsHex(item.getDescription()) + ":" +
                    this.rulebook.getRulebookId();
            item.setId(id);
            return item;

        } else {
            throw new UxException("RULE_OR_INDEX_INVALID: " + rule);

        }
    }

    private void addInterpretations(RulebookItem item, String rule) throws UxException {
        char[] chars = rule.toCharArray();
        int interpretations = 0;
        for (char c : chars){
            if (c=='_'){
                interpretations++;
            }
        }
        if (interpretations%2!=0){
            throw new UxException(
                    "Rule is invalid. An interpretation denoted by the '_' character is opened but not closed:\n"
                            + rule);

        }
        interpretations /= 2;

        for (int i = 0; i<interpretations; i++){
            Interpretation j = new Interpretation();
            j.setDefinition("");
            j.setModifier(Rulebook.MODIFIER_PUBLIC);
            item.getInterpretations().add(j);
        }
        String[] parts = rule.split("]");
        for (int i=1; i< parts.length;i++){
            String iso = parts[i].split("_")[0];
            item.getInterpretations().get(i-1).setDefinition(iso);

        }
    }

    public void addRule(String ruleToAdd) throws UxException {
        int index = this.rulebook.getRules().size() + this.rulebook.getRuleExtensions().size() - 1;
        RulebookItem item = createRule(ruleToAdd, index);
        this.rulebook.getRuleExtensions().add(item);

    }
}


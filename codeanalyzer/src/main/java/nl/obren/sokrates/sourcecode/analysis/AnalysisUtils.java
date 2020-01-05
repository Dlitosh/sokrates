/*
 * Copyright (c) 2019 Željko Obrenović. All rights reserved.
 */

package nl.obren.sokrates.sourcecode.analysis;

import nl.obren.sokrates.common.utils.ProgressFeedback;
import nl.obren.sokrates.sourcecode.SearcheableFilesCache;
import nl.obren.sokrates.sourcecode.analysis.results.AspectAnalysisResults;
import nl.obren.sokrates.sourcecode.aspects.CrossCuttingConcern;
import nl.obren.sokrates.sourcecode.aspects.NamedSourceCodeAspect;
import nl.obren.sokrates.sourcecode.aspects.SourceCodeAspectUtils;
import nl.obren.sokrates.sourcecode.metrics.MetricsList;
import nl.obren.sokrates.sourcecode.metrics.NumericMetric;
import nl.obren.sokrates.sourcecode.search.SearchExpression;
import nl.obren.sokrates.sourcecode.search.SearchRequest;
import nl.obren.sokrates.sourcecode.search.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.file.FileSystems;
import java.text.DecimalFormat;

public class AnalysisUtils {
    private static final Log LOG = LogFactory.getLog(AnalysisUtils.class);

    public static void analyze(String group, NamedSourceCodeAspect aspect, ProgressFeedback progressFeedback, AspectAnalysisResults aspectAnalysisResults, MetricsList metricsList, StringBuffer textSummary, long start) {
        aspectAnalysisResults.setAspect(aspect);
        String prefix = StringUtils.isNotBlank(group) ? (group + FileSystems.getDefault().getSeparator()) : "";

        String name = prefix + aspect.getName();

        metricsList.addMetric()
                .id(getMetricId("NUMBER_OF_FILES_" + name))
                .value(aspect.getSourceFiles().size());

        metricsList.addMetric()
                .id(getMetricId("LINES_OF_CODE_" + name))
                .value(aspect.getLinesOfCode());

        detailedInfo(textSummary, progressFeedback, aspect.getSourceFiles().size() + " are in the " + name + "'s scope "
                + " (" + aspect.getLinesOfCode() + " lines of code)", start);

        aspectAnalysisResults.setFilesCount(aspect.getSourceFiles().size());
        aspectAnalysisResults.setLinesOfCode(aspect.getLinesOfCode());

        if (aspect instanceof CrossCuttingConcern) {
            LOG.info("Creating searchable file chache for " + name);
            SearcheableFilesCache searcheableFilesCache = SearcheableFilesCache.getInstance(aspect.getSourceFiles());
            aspect.getSourceFileFilters().forEach(filter -> {
                SearchRequest searchRequest = new SearchRequest(
                        new SearchExpression(filter.getPathPattern()),
                        new SearchExpression(filter.getContentPattern()));
                LOG.info("Searching for path line \"" + searchRequest.getPathSearchExpression().getExpression() + "\" and/or content like \""
                        + searchRequest.getContentSearchExpression().getExpression() + "\"");
                SearchResult searchResult = searcheableFilesCache.search(searchRequest, new ProgressFeedback());
                aspectAnalysisResults.setNumberOfRegexLineMatches(aspectAnalysisResults.getNumberOfRegexLineMatches()
                        + searchResult.getTotalNumberOfMatchingLines());
            });
        }

        SourceCodeAspectUtils.getAspectsPerExtensions(aspect).forEach(aspectPerExtension -> {
            String extensionMetricName = name + "_" + aspectPerExtension.getName().replace("*.", "");
            metricsList.addMetric()
                    .id(getMetricId("NUMBER_OF_FILES_" + extensionMetricName))
                    .value(aspectPerExtension.getSourceFiles().size());

            metricsList.addMetric()
                    .id(getMetricId("LINES_OF_CODE_" + extensionMetricName))
                    .value(aspectPerExtension.getLinesOfCode());

            detailedInfo(textSummary, progressFeedback, aspectPerExtension.getName() + ": " + aspectPerExtension.getSourceFiles().size() + " files, (" + aspectPerExtension.getLinesOfCode() + " lines of code)", start);
            aspectAnalysisResults.getFileCountPerExtension().add(new NumericMetric(aspectPerExtension.getName(), aspectPerExtension.getSourceFiles().size()));
            aspectAnalysisResults.getLinesOfCodePerExtension().add(new NumericMetric(aspectPerExtension.getName(), aspectPerExtension.getLinesOfCode()));
        });
    }

    public static String getMetricId(String name) {
        name = name.replace(" ", "_").replace("-", "_");
        name = name.replaceAll("[(].*?[)]", "");
        while (name.contains("__")) {
            name = name.replace("__", "_");
        }
        name = StringUtils.removeEnd(name, "_");
        return name.trim().toUpperCase();
    }


    public static void info(StringBuffer textSummary, ProgressFeedback progressFeedback, String line, long start) {
        DecimalFormat formatter = new DecimalFormat("#.00");
        LOG.info(formatter.format(((System.currentTimeMillis() - start) / 10) * 0.01) + "s\t\t" + line.replaceAll("<.*?>", ""));
        textSummary.append(line + "\n");
        if (progressFeedback != null) {
            progressFeedback.setText(line);
        }
    }

    public static void detailedInfo(StringBuffer textSummary, ProgressFeedback progressFeedback, String line, long start) {
        DecimalFormat formatter = new DecimalFormat("#.00");
        LOG.info(formatter.format(((System.currentTimeMillis() - start) / 10) * 0.01) + "s\t\t" + line.replaceAll("<.*?>", ""));
        textSummary.append(line + "\n");
        if (progressFeedback != null) {
            progressFeedback.setDetailedText(line);
        }
    }


}

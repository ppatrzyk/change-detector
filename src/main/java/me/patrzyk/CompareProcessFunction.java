package me.patrzyk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;

public class CompareProcessFunction extends RichFlatMapFunction<Entry, ProcessedEntry> {

    private transient ValueState<Entry> previousEntryStore;

    @Override
    public void open(Configuration config) {
        ValueStateDescriptor<Entry> descriptor =
            new ValueStateDescriptor<>("previousEntryStore", TypeInformation.of(new TypeHint<Entry>() {})); 
        previousEntryStore = getRuntimeContext().getState(descriptor);
    }

    @Override
    public void flatMap(Entry entry, Collector<ProcessedEntry> out) throws Exception {
        Entry previousEntry = previousEntryStore.value();
        Date previousTs = null;
        String previousContent = "";
        List<String[]> diff = new ArrayList<String[]>();
        if (previousEntry != null) {
            previousTs = previousEntry.getTs();
            previousContent = previousEntry.getContent();
        }
        var content = entry.getContent();
        if (!previousContent.equals(content)) {
            List<String> previousContentSplit = Arrays.asList(previousContent.split("\\n"));
            List<String> contentSplit = Arrays.asList(content.split("\\n"));
            DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .oldTag(f -> "~")
                .newTag(f -> "**")
                .build();
            List<DiffRow> rows = generator.generateDiffRows(previousContentSplit, contentSplit);
            for (DiffRow row : rows) {
                String[] diffLine = {row.getOldLine(), row.getNewLine(), };
                diff.add(diffLine);
            }
            var detect = new ProcessedEntry(entry.getTs(), previousTs, content, previousContent, diff);
            previousEntryStore.update(entry);
            out.collect(detect);
        }
    }
    
}

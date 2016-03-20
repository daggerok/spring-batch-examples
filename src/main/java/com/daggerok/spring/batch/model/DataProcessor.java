package com.daggerok.spring.batch.model;

import com.daggerok.spring.batch.util.Sleep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class DataProcessor implements ItemProcessor<Data, Data> {
    /**
     * Process the provided item, returning a potentially modified or new item for continued
     * processing.  If the returned result is null, it is assumed that processing of the item
     * should not continue.
     *
     * @param item to be processed
     * @return potentially modified or new item for continued processing, null if processing of the
     * provided item should not continue.
     * @throws Exception
     */
    @Override
    public Data process(Data item) throws Exception {
        log.info("DataProcessor.process\n{}", item);

        Sleep.some();

        return Data.of("transformed ".concat(item.id.toUpperCase())).setTime(item.time);
    }
}

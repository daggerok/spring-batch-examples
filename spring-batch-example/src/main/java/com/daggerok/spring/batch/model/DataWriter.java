package com.daggerok.spring.batch.model;

import com.daggerok.spring.batch.util.Sleep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@Slf4j
public class DataWriter implements ItemWriter<Data> {
    /**
     * Process the supplied data element. Will not be called with any null items
     * in normal operation.
     *
     * @param items items to be written
     * @throws Exception if there are errors. The framework will catch the
     *                   exception and convert or rethrow it as appropriate.
     */
    @Override
    public void write(List<? extends Data> items) throws Exception {
        Sleep.some();

        log.info("DataWriter.write..\nwritten: {}", items);
    }
}

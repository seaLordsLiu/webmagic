package us.codecraft.webmagic.pipeline;

import us.codecraft.webmagic.PageResult;
import us.codecraft.webmagic.Task;

/**
 * Pipeline is the persistent and offline process part of crawler.<br>
 * The interface Pipeline can be implemented to customize ways of persistent.
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 * @see ConsolePipeline
 */
public interface Pipeline {

    /**
     * Process extracted results.
     *
     * @param resultItems resultItems
     * @param task task
     */
    void process(String result, Task task);
}

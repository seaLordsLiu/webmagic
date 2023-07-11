package us.codecraft.webmagic.pipeline;

import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.Map;

/**
 * Write results in console.<br>
 * Usually used in test.
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
public class ConsolePipeline implements Pipeline {

    @Override
    public void process(String result, Task task) {
        System.out.println("get page: " + task.getUUID());
        System.out.println("result: " + result);
    }
}

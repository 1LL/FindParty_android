package ga.findparty.findparty.util;

import java.io.Serializable;

/**
 * Created by tw on 2017-05-05.
 */

public interface ReviewSelectListener extends Serializable {
    void select(int fragmentPosition, String answer, int selectAnswerIndex);
}

package jewelrock.irev.com.jewelrock.navigators;

import jewelrock.irev.com.jewelrock.model.Video;

/**
 * Created by Yuri Peremetov on 31.08.2017.
 */

public interface AllVideoActions {
    void changeLikeStatusOfVideo(Video video);
    void deleteVideo(Video video);
    void buyVideo(Video video);
    void downloadVideo(Video video);
    void openVideo(Video video);
}

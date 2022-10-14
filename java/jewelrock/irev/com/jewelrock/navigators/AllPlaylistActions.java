package jewelrock.irev.com.jewelrock.navigators;

import jewelrock.irev.com.jewelrock.model.Playlist;

/**
 * Created by Alekseev Igor on 01.04.2019.
 */

public interface AllPlaylistActions {
    void deletePlaylist(Playlist playlist);
    void buyPlaylist(Playlist playlist);
    void downloadPlaylist(Playlist playlist);
    void openPlaylist(Playlist playlist);
}
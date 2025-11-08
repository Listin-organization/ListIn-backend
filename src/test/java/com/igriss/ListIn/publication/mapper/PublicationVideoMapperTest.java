package com.igriss.ListIn.publication.mapper;

import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.entity.PublicationVideo;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PublicationVideoMapperTest {

    private final PublicationVideoMapper mapper = new PublicationVideoMapper();

    @Test
    void test_toProductVideo_withValidUrl() {

        Publication publication = Instancio.create(Publication.class);

        String url = "https://cdn.example.com/videos/test-video.mp4";

        PublicationVideo video = mapper.toProductVideo(url, publication);

        assertThat(video).isNotNull();
        assertThat(video.getPublication()).isEqualTo(publication);
        assertThat(video.getVideoUrl()).isEqualTo(url);
        assertThat(video.getVideoName()).isEqualTo("test-video.mp4");
    }

    @Test
    void test_toProductVideo_urlWithoutSlash() {

        Publication publication = Instancio.create(Publication.class);

        String url = "video123.mp4";

        PublicationVideo video = mapper.toProductVideo(url, publication);

        assertThat(video).isNotNull();
        assertThat(video.getVideoName()).isEqualTo("video123.mp4");
        assertThat(video.getVideoUrl()).isEqualTo(url);
    }

    @Test
    void test_toProductVideo_nullUrl() {
        Publication publication = Instancio.create(Publication.class);

        assertThatThrownBy(() ->
                mapper.toProductVideo(null, publication)
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void test_toProductVideo_nullPublication() {

        String url = "https://cdn.example.com/video.mov";

        PublicationVideo video = mapper.toProductVideo(url, null);

        assertThat(video).isNotNull();
        assertThat(video.getPublication()).isNull();
        assertThat(video.getVideoName()).isEqualTo("video.mov");
    }

}

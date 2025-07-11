package com.igriss.ListIn.chunker_client;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ChunkerResponse {

    private List<FileData> files;

    public List<FileData> getM3u8Files() {
        return files.stream()
                .filter(f -> f.getName().endsWith(".m3u8"))
                .toList();
    }

    public List<FileData> getTsFiles() {
        return files.stream()
                .filter(f -> f.getName().endsWith(".ts"))
                .toList();

    }

}

package com.brasens.utilities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileDownload {
    private String fileName;
    private String filePath;
    private String fileURL;

    public enum FileType{
        SOFTWARE_UPDATE
    }

}

////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2018 Applied Visions - http://securedecisions.com
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.framework.engine.full;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TemporaryExtractionLocation {

    File sourceZipFile, outputDirectory, outputSubDirectory;

    public static boolean isArchive(String filePath) {
        String ext = FilenameUtils.getExtension(filePath).toLowerCase();
        return
            ext.equals("zip") ||
            ext.equals("war");
    }

    public TemporaryExtractionLocation(String sourceZipFile) {
        this(sourceZipFile, System.getProperty("java.io.tmpdir"));
    }

    public TemporaryExtractionLocation(String sourceZipFile, String outputDirectory) {
        this.sourceZipFile = new File(sourceZipFile);
        this.outputDirectory = new File(outputDirectory);

        this.outputSubDirectory = new File(outputDirectory, FilenameUtils.getBaseName(sourceZipFile));
        for (int i = 1; i < 1000 && this.outputSubDirectory.exists(); i++) {
            this.outputSubDirectory = new File(outputDirectory, FilenameUtils.getBaseName(sourceZipFile) + "-" + i);
        }
    }

    public File getOutputPath() {
        return this.outputSubDirectory;
    }

    public void extract() {
        try
        {
            // Create a sub-folder within the target to contain the files
            this.outputSubDirectory.mkdirs();

            int BUFFER = 2048;

            ZipFile zip = new ZipFile(this.sourceZipFile);
            Enumeration zipFileEntries = zip.entries();

            // Process each entry
            while (zipFileEntries.hasMoreElements())
            {
                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();

                File destFile = new File(this.outputSubDirectory, currentEntry);
                File destinationParent = destFile.getParentFile();

                // create the parent directory structure if needed
                destinationParent.mkdirs();

                if (!entry.isDirectory())
                {
                    BufferedInputStream is = new BufferedInputStream(zip
                        .getInputStream(entry));
                    int currentByte;
                    // establish buffer for writing file
                    byte data[] = new byte[BUFFER];

                    // write the current file to disk
                    FileOutputStream fos = new FileOutputStream(destFile);
                    BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

                    // read and write until last byte is encountered
                    while ((currentByte = is.read(data, 0, BUFFER)) != -1)
                    {
                        dest.write(data, 0, currentByte);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                    fos.flush();
                    fos.close();

                }
            }
        }
        catch (Exception e)
        {
        }
    }

    public void release() {
        this.outputSubDirectory.deleteOnExit();
    }

}

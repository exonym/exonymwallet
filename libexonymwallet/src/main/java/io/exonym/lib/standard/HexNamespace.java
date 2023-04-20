package io.exonym.lib.standard;

import io.exonym.lib.exceptions.HubException;

import java.util.logging.Logger;

public class HexNamespace {

    private final static Logger logger = Logger.getLogger(HexNamespace.class.getName());

    public enum UNIT {B, KB, MB};
    public final int MB_TO_B = 1024 * 1024;
    public final int KB_TO_B = 1024;
    private int targetUncompressedSizeB = 128 * MB_TO_B;
    private int estimatedFileSizeKB = 52 * KB_TO_B;

    private int maxRecordsPerFile = 1500;

    private long namespaceDivisions = 1;

    private long expectedArchives = 0;

    private long currentArchiveIndex = 0;

    private String currentArchiveHex;

    public HexNamespace() {

    }

    public long setTotalRecordSize(long records) throws Exception {
        maxRecordsPerFile = targetUncompressedSizeB / estimatedFileSizeKB;
        if (records > namespaceDivisions(6)){
            throw new Exception("Namespace overflow!  The maximum number of records for these namespaces is " + namespaceDivisions(6));

        } else if (records > namespaceDivisions(5)){
            this.namespaceDivisions = 6;

        } else if (records > namespaceDivisions(4)){
            this.namespaceDivisions = 5;

        } else if (records > namespaceDivisions(3)){
            this.namespaceDivisions = 4;

        } else if (records > namespaceDivisions(2)){
            this.namespaceDivisions = 3;

        } else if (records > namespaceDivisions(1)){
            this.namespaceDivisions = 2;

        } else if (records > namespaceDivisions(0)){
            this.namespaceDivisions = 1;

        } else {
            this.namespaceDivisions = 0;

        }
        this.expectedArchives = (int)(Math.pow(16,namespaceDivisions));
        logger.info("------------");
        logger.info("- Creating namespaces for " + records + " files.");
        logger.info("- Target uncompressed zip file size: \t" + targetUncompressedSizeB);
        logger.info("- Based on in-archive file size: \t" + estimatedFileSizeKB);
        logger.info("- Max files per archive: \t" + maxRecordsPerFile);
        logger.info("- Estimated files per archive: \t" + (int)(records/Math.pow(16,namespaceDivisions)));
        logger.info("- Expected number of archives: \t" + expectedArchives);
        logger.info("- Number of namespace divisions: \t" + namespaceDivisions);
        logger.info("------------");
        return this.namespaceDivisions;

    }

    public String nextArchiveName() throws HubException {
        if (expectedArchives==1){
            this.currentArchiveHex = "all";
            throw new HubException("Only one archive expected");

        } else {
            String result = Form.toHex(this.namespaceDivisions, currentArchiveIndex);
            currentArchiveIndex++;
            if (currentArchiveIndex>expectedArchives){
                throw new HubException("Last value of index " + currentArchiveIndex+ " / " + expectedArchives);

            }
            this.currentArchiveHex = result;

        }
        return currentArchiveHex;

    }

    public String currentArchivePath(){
        if (this.currentArchiveHex.equals("all")){
            return "/";

        } else {
            char[] c = this.currentArchiveHex.toCharArray();
            int len = c.length-1;
            StringBuilder builder = new StringBuilder();
            builder.append("/");
            for (int i = 0; i< len; i++){
                builder.append(c[i] + "/");

            }
            return builder.toString();

        }
    }

    /**
     * If you have the last successful archive index you need to increment it before you set this.
     *
     * Also call setTotalRecord size before you call this.
     *
     * @param index
     */
    public void setCurrentIndex(long index){
        this.currentArchiveIndex = index;

    }

    public String getCurrentArchiveHex() {
        return currentArchiveHex;
    }

    private long namespaceDivisions(int namespaceLength) {
        return (long) (maxRecordsPerFile * Math.pow(16, namespaceLength));

    }

    public void setTargetUncompressedSize(int targetSize, UNIT unit){
        if (unit==UNIT.MB){
            targetUncompressedSizeB = targetSize * MB_TO_B;

        } else if (unit==UNIT.KB){
            targetUncompressedSizeB = targetSize * KB_TO_B;

        } else if (unit==UNIT.B){
            targetUncompressedSizeB = targetSize;

        }
    }

    public long getCurrentArchiveIndex() {
        return currentArchiveIndex;
    }

    public void setEstimatedFileSize(int estimatedSize, UNIT unit){
        if (unit==UNIT.MB){
            estimatedFileSizeKB = estimatedSize * MB_TO_B;

        } else if (unit==UNIT.KB){
            estimatedFileSizeKB = estimatedSize * KB_TO_B;

        } else if (unit==UNIT.B){
            estimatedFileSizeKB = estimatedSize;

        }
    }


}

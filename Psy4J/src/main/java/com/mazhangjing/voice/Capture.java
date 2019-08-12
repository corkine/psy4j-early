package com.mazhangjing.voice;

import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;

/**
 * 捕获声音的 JavaFx 任务类
 * @author Corkine Ma
 * @apiNote 2019年3月11日 撰写本类
 *          2019年03月11日 更新了 Mac OS 兼容性 - 更改 channels 为 1
 */
public class Capture extends Task<Double> {

    private final Logger logger = LoggerFactory.getLogger(Capture.class);

    /**
     * 传入的参数，包括编码、比特率、采样率、通道，以此构造 AudioFormat
     */
    private AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
    //44100
    private float rate = 11025;
    //16
    private int sampleSize = 16;
    //Mac Mini 18 不支持双通道格式？
    private int channels = 1;
    //true
    private boolean bigEndian = true;
    private AudioFormat format;

    TargetDataLine line;
    transient Boolean running;

    /**
     * 开始捕获声音
     */
    public void start() {
        running = true;
        updateValue(0.0);
        doIt();
    }

    /**
     * 声音捕获主程序
     */
    private void doIt() {
        format = new AudioFormat(encoding, rate, sampleSize,
                channels, (sampleSize/8)*channels, rate, bigEndian);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info))
            throw new RuntimeException("音频格式不被支持");
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format, line.getBufferSize());
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int frameSizeInBytes = format.getFrameSize();
        int bufferLengthInFrames = line.getBufferSize() / 8;
        int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
        byte[] data = new byte[bufferLengthInBytes];
        int numBytesRead;

        line.start();

        logger.info("Starting Line now...");

        while (running) {
            line.read(data, 0, bufferLengthInBytes);
            double result = dataNow(data);
            updateMessage(String.valueOf(result));
            if (result != 0.0) {
                //System.out.println(result);
            }
        }

        line.stop();
        line.close();
        line = null;
        updateValue(1.0);
    }

    /**
     * 将捕获到的数据流转换成结果
     * @param audioBytes 输入的数据流
     * @return 结果
     */
    private double dataNow(byte[] audioBytes) {
        int h = 300;
        int w = 1;

        int nlengthInSamples = audioBytes.length / 2;
        int[] audioData = new int[nlengthInSamples];
        for (int i = 0; i < nlengthInSamples; i++) {
            /* First byte is MSB (high order) */
            int MSB = (int) audioBytes[2*i];
            /* Second byte is LSB (low order) */
            int LSB = (int) audioBytes[2*i+1];
            audioData[i] = MSB << 8 | (255 & LSB);
        }
        double height = 0.0;
        int frames_per_pixel = audioBytes.length / format.getFrameSize()/w;
        byte my_byte = 0;
        int numChannels = format.getChannels();
        for (double x = 0; x < w; x++) {
            int idx = (int) (frames_per_pixel * numChannels * x);
            if (format.getSampleSizeInBits() == 8) {
                my_byte = (byte) audioData[idx];
            } else {
                my_byte = (byte) (128 * audioData[idx] / 32768 );
            }
            height = (double) (h * (128 - my_byte) / 256);
        }
        return height - 150.0;
    }

    /**
     * 停止捕获声音
     */
    public void stop() {
        running = false;
    }

    @Override
    protected Double call() throws Exception {
        this.start();
        logger.info("At Call End");
        return null;
    }
}

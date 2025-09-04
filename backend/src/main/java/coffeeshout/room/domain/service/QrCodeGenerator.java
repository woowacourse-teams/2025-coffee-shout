package coffeeshout.room.domain.service;

import com.google.zxing.WriterException;
import java.io.IOException;

public interface QrCodeGenerator {

    byte[] generate(String url) throws WriterException, IOException;
}

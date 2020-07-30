/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tf.search.lucene.codecs;



import com.tf.search.lucene.store.DataInput;
import com.tf.search.lucene.store.DataOutput;
import com.tf.search.lucene.utils.BytesRef;
import com.tf.search.lucene.utils.StringHelper;
import java.io.IOException;

/**
 * Utility class for reading and writing versioned headers.
 * <p>
 * Writing codec headers is useful to ensure that a file is in 
 * the format you think it is.
 * 
 * @lucene.experimental
 */

public final class CodecUtil {
  private CodecUtil() {} // no instance

  /**
   * Constant to identify the start of a codec header.
   */
  public final static int CODEC_MAGIC = 0x3fd76c17;
  /**
   * Constant to identify the start of a codec footer.
   */
  public final static int FOOTER_MAGIC = ~CODEC_MAGIC;

  /**
   * Writes a codec header, which records both a string to
   * identify the file and a version number. This header can
   * be parsed and validated with 
   * {@link #checkHeader(DataInput, String, int, int) checkHeader()}.
   * <p>
   * CodecHeader --&gt; Magic,CodecName,Version
   * <ul>
   *    <li>Magic --&gt; {@link DataOutput#writeInt Uint32}. This
   *        identifies the start of the header. It is always {@value #CODEC_MAGIC}.
   *    <li>CodecName --&gt; {@link DataOutput#writeString String}. This
   *        is a string to identify this file.
   *    <li>Version --&gt; {@link DataOutput#writeInt Uint32}. Records
   *        the version of the file.
   * </ul>
   * <p>
   * Note that the length of a codec header depends only upon the
   * name of the codec, so this length can be computed at any time
   * with {@link #headerLength(String)}.
   * 
   * @param out Output stream
   * @param codec String to identify this file. It should be simple ASCII, 
   *              less than 128 characters in length.
   * @param version Version number
   * @throws IOException If there is an I/O error writing to the underlying medium.
   * @throws IllegalArgumentException If the codec name is not simple ASCII, or is more than 127 characters in length
   */
  public static void writeHeader(DataOutput out, String codec, int version) throws IOException {
    BytesRef bytes = new BytesRef(codec);
    if (bytes.length != codec.length() || bytes.length >= 128) {
      throw new IllegalArgumentException("codec must be simple ASCII, less than 128 characters in length [got " + codec + "]");
    }
    out.writeInt(CODEC_MAGIC);
    out.writeString(codec);
    out.writeInt(version);
  }
  
  /**
   * Writes a codec header for an index file, which records both a string to
   * identify the format of the file, a version number, and data to identify
   * the file instance (ID and auxiliary suffix such as generation).
   * <p>
   * This header can be parsed and validated with 
   * {@link #checkIndexHeader(DataInput, String, int, int, byte[], String) checkIndexHeader()}.
   * <p>
   * IndexHeader --&gt; CodecHeader,ObjectID,ObjectSuffix
   * <ul>
   *    <li>CodecHeader   --&gt; {@link #writeHeader}
   *    <li>ObjectID     --&gt; {@link DataOutput#writeByte byte}<sup>16</sup>
   *    <li>ObjectSuffix --&gt; SuffixLength,SuffixBytes
   *    <li>SuffixLength  --&gt; {@link DataOutput#writeByte byte}
   *    <li>SuffixBytes   --&gt; {@link DataOutput#writeByte byte}<sup>SuffixLength</sup>
   * </ul>
   * <p>
   * Note that the length of an index header depends only upon the
   * name of the codec and suffix, so this length can be computed at any time
   * with {@link #indexHeaderLength(String,String)}.
   * 
   * @param out Output stream
   * @param codec String to identify the format of this file. It should be simple ASCII, 
   *              less than 128 characters in length.
   * @param id Unique identifier for this particular file instance.
   * @param suffix auxiliary suffix information for the file. It should be simple ASCII,
   *              less than 256 characters in length.
   * @param version Version number
   * @throws IOException If there is an I/O error writing to the underlying medium.
   * @throws IllegalArgumentException If the codec name is not simple ASCII, or 
   *         is more than 127 characters in length, or if id is invalid,
   *         or if the suffix is not simple ASCII, or more than 255 characters
   *         in length.
   */
  public static void writeIndexHeader(DataOutput out, String codec, int version, byte[] id, String suffix) throws IOException {
    if (id.length != StringHelper.ID_LENGTH) {
      throw new IllegalArgumentException("Invalid id: " + StringHelper.idToString(id));
    }
    writeHeader(out, codec, version);
    out.writeBytes(id, 0, id.length);
    BytesRef suffixBytes = new BytesRef(suffix);
    if (suffixBytes.length != suffix.length() || suffixBytes.length >= 256) {
      throw new IllegalArgumentException("suffix must be simple ASCII, less than 256 characters in length [got " + suffix + "]");
    }
    out.writeByte((byte) suffixBytes.length);
    out.writeBytes(suffixBytes.bytes, suffixBytes.offset, suffixBytes.length);
  }

  /**
   * Computes the length of a codec header.
   * 
   * @param codec Codec name.
   * @return length of the entire codec header.
   * @see #writeHeader(DataOutput, String, int)
   */
  public static int headerLength(String codec) {
    return 9+codec.length();
  }
  
  /**
   * Computes the length of an index header.
   * 
   * @param codec Codec name.
   * @return length of the entire index header.
   * @see #writeIndexHeader(DataOutput, String, int, byte[], String)
   */
  public static int indexHeaderLength(String codec, String suffix) {
    return headerLength(codec) + StringHelper.ID_LENGTH + 1 + suffix.length();
  }

  /**
   * Reads and validates a header previously written with 
   * {@link #writeHeader(DataOutput, String, int)}.
   * <p>
   * When reading a file, supply the expected <code>codec</code> and
   * an expected version range (<code>minVersion to maxVersion</code>).
   * 
   * @param in Input stream, positioned at the point where the
   *        header was previously written. Typically this is located
   *        at the beginning of the file.
   * @param codec The expected codec name.
   * @param minVersion The minimum supported expected version number.
   * @param maxVersion The maximum supported expected version number.
   * @return The actual version found, when a valid header is found 
   *         that matches <code>codec</code>, with an actual version 
   *         where {@code minVersion <= actual <= maxVersion}.
   *         Otherwise an exception is thrown.
   * @throws CorruptIndexException If the first four bytes are not
   *         {@link #CODEC_MAGIC}, or if the actual codec found is
   *         not <code>codec</code>.
   * @throws IndexFormatTooOldException If the actual version is less 
   *         than <code>minVersion</code>.
   * @throws IndexFormatTooNewException If the actual version is greater 
   *         than <code>maxVersion</code>.
   * @throws IOException If there is an I/O error reading from the underlying medium.
   * @see #writeHeader(DataOutput, String, int)
   */
  public static int checkHeader(DataInput in, String codec, int minVersion, int maxVersion) throws IOException {
    // Safety to guard against reading a bogus string:
    final int actualHeader = in.readInt();
    if (actualHeader != CODEC_MAGIC) {
      throw new CorruptIndexException("codec header mismatch: actual header=" + actualHeader + " vs expected header=" + CODEC_MAGIC, in);
    }
    return checkHeaderNoMagic(in, codec, minVersion, maxVersion);
  }

  /** Like {@link
   *  #checkHeader(DataInput,String,int,int)} except this
   *  version assumes the first int has already been read
   *  and validated from the input. */
  public static int checkHeaderNoMagic(DataInput in, String codec, int minVersion, int maxVersion) throws IOException {
    final String actualCodec = in.readString();
    if (!actualCodec.equals(codec)) {
      throw new CorruptIndexException("codec mismatch: actual codec=" + actualCodec + " vs expected codec=" + codec, in);
    }

    final int actualVersion = in.readInt();
    if (actualVersion < minVersion) {
      throw new IndexFormatTooOldException(in, actualVersion, minVersion, maxVersion);
    }
    if (actualVersion > maxVersion) {
      throw new IndexFormatTooNewException(in, actualVersion, minVersion, maxVersion);
    }

    return actualVersion;
  }


}

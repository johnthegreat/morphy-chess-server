/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2017 http://code.google.com/p/morphy-chess-server/
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package morphy.timeseal;

import java.util.Arrays;

public class TimesealCoder extends Object {
	public static byte[] initialTimesealStringBytes = "TIMESEAL2|OpenSeal|OpenSeal|".getBytes();
	
	// This key cannot change. It is presumably hard-coded throughout all Timeseal clients and servers.
	private final byte timesealKey[] = "Timestamp (FICS) v1.0 - programmed by Henrik Gram.".getBytes();
	
	/*
	 * 
	 * Notes on timeseal:
	 * 
	 * You do not send the full timestamp since epoch.
	 * You will send the time difference from now compared to when you first connected to the server.
	 * 
	 * FICS will send a "[G]\0" periodically, maybe more than once. You are *REQUIRED* to ACK *every time* one is received.
	 * The ACK will be "\0029\n" in Timeseal-encoded format.
	 * 
	 */

	//
	// This method adapted from the Raptor Fics Interface project
	// New BSD License.
	// https://github.com/Raptor-Fics-Interface/Raptor/blob/master/raptor/src/raptor/connector/ics/timeseal/TimesealSocketMessageProducer.java
	//
	public byte[] encode(byte stringToWriteBytes[], long timestamp) {
		byte[] buffer = new byte[10000];
		
		int bytesInLength = stringToWriteBytes.length;
		System.arraycopy(stringToWriteBytes, 0, buffer, 0, stringToWriteBytes.length);
		buffer[bytesInLength++] = 24; // \u0024
		byte abyte1[] = Long.toString(timestamp).getBytes();
		System.arraycopy(abyte1, 0, buffer, bytesInLength, abyte1.length);
		bytesInLength += abyte1.length;
		buffer[bytesInLength++] = 25; // \u0025
		int j = bytesInLength;
		for (bytesInLength += 12 - bytesInLength % 12; j < bytesInLength;) {
			buffer[j++] = 49;
		}

		for (int k = 0; k < bytesInLength; k++) {
			buffer[k] |= 0x80; // 128
		}

		for (int i1 = 0; i1 < bytesInLength; i1 += 12) {
			byte byte0 = buffer[i1 + 11];
			buffer[i1 + 11] = buffer[i1];
			buffer[i1] = byte0;
			byte0 = buffer[i1 + 9];
			buffer[i1 + 9] = buffer[i1 + 2];
			buffer[i1 + 2] = byte0;
			byte0 = buffer[i1 + 7];
			buffer[i1 + 7] = buffer[i1 + 4];
			buffer[i1 + 4] = byte0;
		}

		int l1 = 0;
		for (int j1 = 0; j1 < bytesInLength; j1++) {
			buffer[j1] ^= timesealKey[l1];
			l1 = (l1 + 1) % timesealKey.length;
		}

		for (int k1 = 0; k1 < bytesInLength; k1++) {
			buffer[k1] -= 32;
		}

		buffer[bytesInLength++] = -0x80; // -128
		buffer[bytesInLength++] = 10;
		
		buffer = Arrays.copyOfRange(buffer, 0, bytesInLength);
		return buffer;
	}
	
	//
	// This method adapted from the Bics Chess project
	// GPL v3 License.
	// https://code.google.com/archive/p/bics-chess/
	// openseal.c
	//
	
	/*public byte[] encode(byte stringToWriteBytes[], long timestamp) {
		byte[] buffer = new byte[10000];
		
		int bytesInLength = stringToWriteBytes.length;
		System.arraycopy(stringToWriteBytes, 0, buffer, 0, stringToWriteBytes.length);
		buffer[bytesInLength++] = 24; // \u0024
		byte abyte1[] = Long.toString(timestamp).getBytes();
		System.arraycopy(abyte1, 0, buffer, bytesInLength, abyte1.length);
		bytesInLength += abyte1.length;
		buffer[bytesInLength++] = 25; // \u0025
		for(;bytesInLength % 12 != 0;bytesInLength++) {
			buffer[bytesInLength] = (byte) '1';
		}
		for(int n=0;n+11<bytesInLength;n+=12) {
			SC(buffer, n, n+11);
			SC(buffer, n+2, n+9);
			SC(buffer, n+4, n+7);
		}
		for(int n=0;n<bytesInLength;n++) {
			buffer[n] = (byte) (((buffer[n] | 0x80) ^ timesealKey[n % timesealKey.length]) - 32);
		}
		buffer[bytesInLength++] = (byte) -0x80;
		buffer[bytesInLength++] = '\n';
		buffer = Arrays.copyOfRange(buffer, 0, bytesInLength);
		return buffer;
	}*/
	
	//
	// This method adapted from the Bics Chess project
	// GPL v3 License.
	// https://code.google.com/archive/p/bics-chess/
	// openseal_decoder.c
	//
	
	public TimesealParseResult decode(byte[] bytes) {
		byte[] key = timesealKey;
		byte[] tmp = bytes;
		
		int n;
		byte offset;
		int l = tmp.length;
		if (l % 12 != 1) {
			// malformed?
		}
		offset = tmp[l-1];
		for(n = 0;n<l;n++) {
			// n+offset+0x80 differs slightly from original C version, it was previously n+offset-0x80
			// but since offset is -128, -128 - 0x80 (128) = -256, so it was not working properly.
			tmp[n] = (byte) ((tmp[n]+32) ^ key[(n+offset+0x80)%key.length]);
			if ((tmp[n] & 0x80) == 0) {
				// malformed?
			}
			tmp[n] ^= 0x80;
		}
		
		for(n=0;n+11<l;n+=12) {
			SC(tmp, n, n+11);
			SC(tmp, n+2, n+9);
			SC(tmp, n+4, n+7);
		}
		
		for(n=0;n < tmp.length;n++) {
			if ((tmp[n] >> 5) == 0) {
				// malformed?
			}
		}
		
		// \x18 = \u0024
		// \x19 = \u0025
		int timeStartIdx = indexOfByte(tmp, (byte) 24);
		int timeEndIdx = indexOfByte(tmp, (byte) 25);
		
		String timestamp = new String(Arrays.copyOfRange(tmp, timeStartIdx+1, timeEndIdx));
		String message = new String(Arrays.copyOfRange(tmp, 0, timeStartIdx));
		
		TimesealParseResult result = new TimesealParseResult(Long.parseLong(timestamp), message);
		return result;
	}
	
	private void SC(byte[] arr, int A,int B) {
		arr[B] ^= arr[A] ^= arr[B];
		arr[A] ^= arr[B];
	}
	
	private int indexOfByte(byte[] bytes, byte b) {
		for(int i=0;i<bytes.length;i++) {
			if (bytes[i] == b) {
				return i;
			}
		}
		return -1;
	}
}
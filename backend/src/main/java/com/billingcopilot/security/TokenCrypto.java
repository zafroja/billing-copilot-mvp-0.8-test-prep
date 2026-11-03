package com.billingcopilot.security;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
/** AES-256-GCM token encryption. Supply 32-byte TOKEN_ENCRYPTION_KEY_B64 via secrets manager. */
public final class TokenCrypto {
 private static final SecureRandom RANDOM=new SecureRandom();
 private TokenCrypto(){}
 private static SecretKeySpec key(){
  String encoded=System.getenv("TOKEN_ENCRYPTION_KEY_B64");
  if(encoded==null || encoded.isBlank()) throw new IllegalStateException("TOKEN_ENCRYPTION_KEY_B64 is required to store OAuth tokens");
  byte[] key;
  try{key=Base64.getDecoder().decode(encoded);}catch(IllegalArgumentException ex){throw new IllegalStateException("Invalid token encryption key encoding");}
  if(key.length!=32)throw new IllegalStateException("TOKEN_ENCRYPTION_KEY_B64 must encode exactly 32 bytes");
  return new SecretKeySpec(key,"AES");
 }
 public static String encrypt(String input){
  if(input==null)return null;
  try{byte[] nonce=new byte[12];RANDOM.nextBytes(nonce);
   Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key(),new GCMParameterSpec(128,nonce));
   byte[] payload=cipher.doFinal(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
   byte[] combined=new byte[nonce.length+payload.length];System.arraycopy(nonce,0,combined,0,nonce.length);System.arraycopy(payload,0,combined,nonce.length,payload.length);
   return "enc:v1:"+Base64.getEncoder().encodeToString(combined);
  }catch(Exception ex){throw new IllegalStateException("OAuth token encryption failed",ex);}
 }
 public static String decrypt(String input){
  if(input==null)return null;
  if(!input.startsWith("enc:v1:"))throw new IllegalStateException("Refusing to read unencrypted legacy Jobber token; reconnect securely");
  try{byte[] combined=Base64.getDecoder().decode(input.substring(7));if(combined.length<29)throw new IllegalStateException("Malformed encrypted OAuth token");
   byte[] nonce=java.util.Arrays.copyOfRange(combined,0,12);byte[] payload=java.util.Arrays.copyOfRange(combined,12,combined.length);
   Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,nonce));
   return new String(cipher.doFinal(payload),java.nio.charset.StandardCharsets.UTF_8);
  }catch(Exception ex){throw new IllegalStateException("Unable to decrypt OAuth token; confirm encryption key",ex);}
 }
}

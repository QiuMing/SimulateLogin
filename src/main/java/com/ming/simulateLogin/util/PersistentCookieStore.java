package com.ming.simulateLogin.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class PersistentCookieStore implements CookieStore {
	private static final Gson gson= new GsonBuilder().setPrettyPrinting().create();
    private static final String COOKIE_NAME_PREFIX = "cookie_";

    private final HashMap<String, ConcurrentHashMap<String, HttpCookie>> cookies;
    private  Map<String,String> cookiePrefs=new HashMap<String, String>();

    /**
     * Construct a persistent cookie store.
     *
     */
    public PersistentCookieStore() {
    	String cookieJson = readFile("cookie.json");
    	Map<String,String> fromJson = gson.fromJson(cookieJson,new TypeToken<Map<String, String>>() {}.getType());  
    	if(fromJson!=null){
    		System.out.println(fromJson);
    		cookiePrefs=fromJson;
    	}
    	  	
    	cookies = new HashMap<String, ConcurrentHashMap<String, HttpCookie>>();

        // Load any previously stored cookies into the store
        for(Map.Entry<String, ?> entry : cookiePrefs.entrySet()) {
            if (((String)entry.getValue()) != null && !((String)entry.getValue()).startsWith(COOKIE_NAME_PREFIX)) {
                String[] cookieNames = split((String) entry.getValue(), ",");
                for (String name : cookieNames) {
                    String encodedCookie = cookiePrefs.get(COOKIE_NAME_PREFIX + name);
                    if (encodedCookie != null) {
                        HttpCookie decodedCookie = decodeCookie(encodedCookie);
                        if (decodedCookie != null) {
                            if(!cookies.containsKey(entry.getKey()))
                                cookies.put(entry.getKey(), new ConcurrentHashMap<String, HttpCookie>());
                            cookies.get(entry.getKey()).put(name, decodedCookie);
                            System.err.println("key---"+entry.getKey()+"  decoded---"+decodedCookie);
                        }
                    }
                }

            }
        }
    }

    public void add(URI uri, HttpCookie cookie) {
        String name = getCookieToken(uri, cookie);

        // Save cookie into local store, or remove if expired
        if (!cookie.hasExpired()) {
            if(!cookies.containsKey(uri.getHost()))
                cookies.put(uri.getHost(), new ConcurrentHashMap<String, HttpCookie>());
            cookies.get(uri.getHost()).put(name, cookie);
        } else {
            if(cookies.containsKey(uri.toString()))
                cookies.get(uri.getHost()).remove(name);
        }
        
       /* System.err.println("add函数，put方法  --uri.getHost---"+uri.getHost()+"   keySet---"+ cookies.get(uri.getHost()).keySet()+"encodeCookie   "
        		+ ""+encodeCookie(new SerializableHttpCookie(cookie)) + cookie);*/
        
        //System.err.println("uri "+uri+" "+"  gethost  "+uri.getHost()+cookie.getName() + " ==  "+cookie.getValue());
        cookiePrefs.put(uri.getHost(), join(",", cookies.get(uri.getHost()).keySet()));
        cookiePrefs.put(COOKIE_NAME_PREFIX + name, encodeCookie(new SerializableHttpCookie(cookie)));
        
         try {
        	 if(PropertieUtil.createPropertiesIfNotExist(uri.getHost()+".cookie.properties"))
        		 PropertieUtil.writeProperties(uri.getHost()+".cookie.properties", cookie.getName(), cookie.getValue());
		} catch (IOException e) {
			e.printStackTrace();
		}
         
        String json=gson.toJson(cookiePrefs);
        saveFile(json.getBytes(), "cookie.json");
        
    }

    protected String getCookieToken(URI uri, HttpCookie cookie) {
        return cookie.getName() + cookie.getDomain();
    }

    public List<HttpCookie> get(URI uri) {
        ArrayList<HttpCookie> ret = new ArrayList<HttpCookie>();
        if(cookies.containsKey(uri.getHost()))
            ret.addAll(cookies.get(uri.getHost()).values());
        return ret;
    }

    public boolean removeAll() {
    	cookiePrefs.clear();
        cookies.clear();
        return true;
    }


    public boolean remove(URI uri, HttpCookie cookie) {
        String name = getCookieToken(uri, cookie);

        if(cookies.containsKey(uri.getHost()) && cookies.get(uri.getHost()).containsKey(name)) {
            cookies.get(uri.getHost()).remove(name);
            if(cookiePrefs.containsKey(COOKIE_NAME_PREFIX + name)) {
            	cookiePrefs.remove(COOKIE_NAME_PREFIX + name);
            }
            cookiePrefs.put(uri.getHost(), join(",", cookies.get(uri.getHost()).keySet()));

            return true;
        } else {
            return false;
        }
    }

    public List<HttpCookie> getCookies() {
        ArrayList<HttpCookie> ret = new ArrayList<HttpCookie>();
        for (String key : cookies.keySet()){
        	System.err.println("getCookies "+"key "+key+"  values  "+cookies.get(key).values());
            ret.addAll(cookies.get(key).values());
        }
        return ret;
    }

    public List<URI> getURIs() {
        ArrayList<URI> ret = new ArrayList<URI>();
        for (String key : cookies.keySet())
            try {
                ret.add(new URI(key));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

        return ret;
    }

    /**
     * Serializes Cookie object into String
     *
     * @param cookie cookie to be encoded, can be null
     * @return cookie encoded as String
     */
    protected String encodeCookie(SerializableHttpCookie cookie) {
        if (cookie == null)
            return null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(cookie);
        } catch (IOException e) {
        	System.out.println("IOException in encodeCookie"+ e);
            return null;
        }

        return byteArrayToHexString(os.toByteArray());
    }

    /**
     * Returns cookie decoded from cookie string
     *
     * @param cookieString string of cookie as returned from http request
     * @return decoded cookie or null if exception occured
     */
    protected HttpCookie decodeCookie(String cookieString) {
        byte[] bytes = hexStringToByteArray(cookieString);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        HttpCookie cookie = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            cookie = ((SerializableHttpCookie) objectInputStream.readObject()).getCookie();
        } catch (IOException e) {
        	System.out.println("IOException in decodeCookie"+e);
        } catch (ClassNotFoundException e) {
        	System.out.println("ClassNotFoundException in decodeCookie"+e);
        }

        return cookie;
    }

    /**
     * Using some super basic byte array &lt;-&gt; hex conversions so we don't have to rely on any
     * large Base64 libraries. Can be overridden if you like!
     *
     * @param bytes byte array to be converted
     * @return string containing hex values
     */
    protected String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte element : bytes) {
            int v = element & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    /**
     * Converts hex values from strings to byte arra
     *
     * @param hexString string of hex-encoded values
     * @return decoded byte array
     */
    protected byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
    public static String join(CharSequence delimiter, Iterable tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token: tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }
    public static String[] split(String text, String expression) {
        if (text.length() == 0) {
            return new String[]{};
        } else {
            return text.split(expression, -1);
        }
    }
    
    public static void saveFile(byte[] bfile, String fileName) {
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		File file = null;
		try {
			file = new File(fileName);
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(bfile);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
    public static String readFile(String fileName) {
		BufferedInputStream bis = null;
		FileInputStream fis = null;
		File file = null;
		try {
			file = new File(fileName);
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
			
			int available = bis.available();
			byte[] bytes=new byte[available];
			bis.read(bytes);
			String str=new String(bytes);
			return str;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return "";
	}
}
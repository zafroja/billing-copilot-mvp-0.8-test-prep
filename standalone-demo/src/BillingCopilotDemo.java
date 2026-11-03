import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.math.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;

/** Standalone, localhost-only Java 17+ demo. No credentials, DB, or network access to Jobber. */
public final class BillingCopilotDemo {
  static final String JOB_ID="SYNTHETIC_NODE_3";
  static final int MAX_BODY=1024*1024;
  final Path directory;
  BillingCopilotDemo(Path directory){this.directory=directory;}
  static Map<String,Object> obj(Object value){if(!(value instanceof Map<?,?> raw))throw new IllegalArgumentException("Expected JSON object"); Map<String,Object> result=new LinkedHashMap<>();raw.forEach((k,v)->result.put(String.valueOf(k),v));return result;}
  static List<Object> arr(Object value){if(!(value instanceof List<?> raw))throw new IllegalArgumentException("Expected JSON array");return new ArrayList<>(raw);}
  static Object prop(Map<String,Object> value,String k){if(!value.containsKey(k) || value.get(k)==null)throw new IllegalArgumentException("Missing "+k);return value.get(k);}
  static Map<String,Object> child(Map<String,Object> value,String k){return obj(prop(value,k));}
  static String string(Map<String,Object> value,String k){return String.valueOf(prop(value,k));}
  static boolean yes(Map<String,Object> value,String k){return Boolean.TRUE.equals(prop(value,k));}
  static BigDecimal dec(Map<String,Object> value,String k){return new BigDecimal(String.valueOf(prop(value,k)));}
  static List<Object> nodes(Map<String,Object> parent,String field){return arr(prop(child(parent,field),"nodes"));}
  static void requireAll(Map<String,Object> parent,String field){Map<String,Object> page=child(child(parent,field),"pageInfo");if(yes(page,"hasNextPage"))throw new IllegalArgumentException("Incomplete "+field+" page; refusing to audit");}
  static void rejectPotentialTruncation(List<Object> values,String what){if(values.size()>=10)throw new IllegalArgumentException("Possible truncated "+what+"; refusing to audit");}
  static String normal(String a){return a.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+"," ").trim();}
  static Map<String,Object> report(String json, boolean approved, String unit, String currency){
    if(!approved) throw new IllegalArgumentException("Approve a per-application contract rate before performing this fixed-price job audit");
    if(!Set.of("application","per application","visit","per visit").contains(unit.toLowerCase(Locale.ROOT)))
      throw new IllegalArgumentException("Only explicitly approved per-application or per-visit rules may be used");
    if(!currency.matches("[A-Z]{3}"))throw new IllegalArgumentException("Currency must be an ISO 4217 code");
    Map<String,Object> root=obj(new Json(json).parse());
    if(root.containsKey("errors") && !arr(root.get("errors")).isEmpty())throw new IllegalArgumentException("GraphQL errors in snapshot");
    Map<String,Object> job=child(child(root,"data"),"job");
    if(!"FIXED_PRICE".equals(string(job,"billingType")))throw new IllegalArgumentException("Only FIXED_PRICE scenario is verified in this demo");
    requireAll(job,"visits");requireAll(job,"invoices");
    List<Object> visits=nodes(job,"visits"),invoices=nodes(job,"invoices");
    if(invoices.size()!=1)throw new IllegalArgumentException("Expected exactly one scoped invoice, found "+invoices.size());
    Map<String,Object> invoice=obj(invoices.get(0));
    if(!"draft".equalsIgnoreCase(string(invoice,"invoiceStatus")))throw new IllegalArgumentException("This demo audits draft invoices only");
    List<Object> invLines=nodes(invoice,"lineItems");rejectPotentialTruncation(invLines,"invoice items");
    String invoiceId=string(invoice,"id");
    Map<String,BigDecimal> done=new TreeMap<>(), billed=new TreeMap<>(), rates=new TreeMap<>();
    Map<String,BigDecimal> invoiceTotals=new TreeMap<>();
    Map<String,List<String>> evidence=new TreeMap<>();
    Set<String> visitIds=new HashSet<>();
    int completed=0;
    for(Object v:visits){Map<String,Object> visit=obj(v); if(!yes(visit,"isComplete"))continue;
      String visitId=string(visit,"id"); if(!visitIds.add(visitId))throw new IllegalArgumentException("Duplicate visit "+visitId);
      if(!invoiceId.equals(string(child(visit,"invoice"),"id")))throw new IllegalArgumentException("Visit associated with another invoice; requires separate scope");
      List<Object> lines=nodes(visit,"lineItems");rejectPotentialTruncation(lines,"visit items");
      completed++;
      for(Object line:lines){Map<String,Object> l=obj(line);String service=normal(string(l,"name"));
        BigDecimal quantity=dec(l,"quantity");if(quantity.signum()<0)throw new IllegalArgumentException("Negative visit quantity");
        done.merge(service,quantity,BigDecimal::add);
        evidence.computeIfAbsent(service,k->new ArrayList<>()).add(visitId);
      }
    }
    for(Object line:invLines){Map<String,Object> l=obj(line);String service=normal(string(l,"name"));
      BigDecimal qty=dec(l,"quantity"),price=dec(l,"unitPrice");
      if(qty.signum()<0||price.signum()<0)throw new IllegalArgumentException("Negative invoice values not supported");
      billed.merge(service,qty,BigDecimal::add);invoiceTotals.merge(service,qty.multiply(price),BigDecimal::add);
    }
    // Rule is independently approved for Salt Application, priced at 75 EUR per application.
    BigDecimal approvedRate=new BigDecimal("75");String approvedService="salt application";
    if(!done.containsKey(approvedService))throw new IllegalArgumentException("Approved service is not present in completed visits");
    BigDecimal performed=done.get(approvedService), invoiced=billed.getOrDefault(approvedService,BigDecimal.ZERO);
    BigDecimal quantityDelta=performed.subtract(invoiced);
    BigDecimal quantityImpact=quantityDelta.multiply(approvedRate);
    BigDecimal approvedInvoiceValue=invoiced.multiply(approvedRate);
    BigDecimal billedValue=invoiceTotals.getOrDefault(approvedService,BigDecimal.ZERO);
    List<Map<String,Object>> discrepancies=new ArrayList<>();
    if(quantityDelta.signum()!=0) discrepancies.add(Map.of("type","QUANTITY_MISMATCH","service","Salt Application","completedQuantity",performed,"invoicedQuantity",invoiced,"quantityDifference",quantityDelta,"potentialImpact",quantityImpact.abs().setScale(2),"sourceVisitIds",evidence.get(approvedService)));
    if(approvedInvoiceValue.compareTo(billedValue)!=0) discrepancies.add(Map.of("type","RATE_MISMATCH","service","Salt Application","potentialImpact",approvedInvoiceValue.subtract(billedValue).abs().setScale(2)));
    BigDecimal total=BigDecimal.ZERO; for(var d:discrepancies)total=total.add((BigDecimal)d.get("potentialImpact"));
    Map<String,Object> out=new LinkedHashMap<>();out.put("jobId",string(job,"id"));out.put("jobNumber",prop(job,"jobNumber"));out.put("invoiceNumber",string(invoice,"invoiceNumber"));out.put("invoiceStatus","draft");out.put("billingType", "FIXED_PRICE");out.put("currency",currency);out.put("currencySource","User-confirmed test account setting; not contained in job JSON");out.put("completedVisits",completed);out.put("completedQuantity",performed);out.put("invoicedQuantity",invoiced);out.put("approvedRate",approvedRate);out.put("expectedValue",performed.multiply(approvedRate).setScale(2));out.put("invoicedValue",billedValue.setScale(2));out.put("potentialImpact",total.setScale(2));out.put("discrepancies",discrepancies);out.put("decision","HUMAN_REVIEW_REQUIRED");out.put("snapshotMode","OFFLINE_VERIFIED_JOBBER_EXPORT");return out;
  }
  void serve(int port)throws IOException{
    HttpServer server=HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(),port),16);
    server.createContext("/",exchange->{try{if(!"GET".equals(exchange.getRequestMethod())){reply(exchange,405,"Method not allowed","text/plain");return;}String path=exchange.getRequestURI().getPath();if(path.equals("/")||path.equals("/index.html"))reply(exchange,200,Files.readString(directory.resolve("web/index.html")),"text/html; charset=utf-8");else if(path.equals("/api/health"))reply(exchange,200,"{\"status\":\"ok\",\"mode\":\"offline\"}","application/json");else reply(exchange,404,"Not found","text/plain");}catch(Exception ex){reply(exchange,500,"Internal error","text/plain");}});
    server.createContext("/api/audit",exchange->{try{
      if(!"POST".equals(exchange.getRequestMethod())){reply(exchange,405,"POST required","text/plain");return;}
      String json=Files.readString(directory.resolve("fixtures/job1-real-response.json"));
      byte[] body=exchange.getRequestBody().readNBytes(MAX_BODY+1);if(body.length>MAX_BODY)throw new IllegalArgumentException("Payload too large");
      Map<String,Object> payload=obj(new Json(new String(body,StandardCharsets.UTF_8)).parse());
      boolean approved=Boolean.TRUE.equals(payload.get("approved"));String unit=String.valueOf(payload.getOrDefault("unit",""));String currency=String.valueOf(payload.getOrDefault("currency","EUR"));
      Map<String,Object> result=report(json,approved,unit,currency);
      exchange.getResponseHeaders().set("Cache-Control","no-store");reply(exchange,200,Json.stringify(result),"application/json");
    }catch(IllegalArgumentException e){reply(exchange,422,Json.stringify(Map.of("error",e.getMessage())),"application/json");}catch(Exception e){reply(exchange,500,Json.stringify(Map.of("error","Unexpected audit failure")),"application/json");}});
    server.setExecutor(Executors.newFixedThreadPool(4));server.start();System.out.println("Billing Copilot offline demo listening at http://127.0.0.1:"+port);System.out.println("Press Ctrl+C to stop. This DOES NOT connect to Jobber.");
  }
  static void reply(HttpExchange ex,int status,String body,String mime)throws IOException{byte[] out=body.getBytes(StandardCharsets.UTF_8);ex.getResponseHeaders().set("Content-Type",mime);ex.getResponseHeaders().set("X-Content-Type-Options","nosniff");ex.getResponseHeaders().set("Content-Security-Policy","default-src 'self' 'unsafe-inline'; img-src 'self' data:");ex.sendResponseHeaders(status,out.length);try(OutputStream os=ex.getResponseBody()){os.write(out);}}
  public static void main(String[]args)throws Exception{
    Path root=Path.of(System.getProperty("user.dir"));
    if(args.length>0 && args[0].equals("--test")){runTests(root);return;}
    int port=args.length>0?Integer.parseInt(args[0]):8787;new BillingCopilotDemo(root).serve(port);
  }
  static void runTests(Path dir)throws Exception{
    String json=Files.readString(dir.resolve("fixtures/job1-real-response.json"));
    Map<String,Object> result=report(json,true,"application","EUR");
    assertEquals(result.get("completedQuantity"),new BigDecimal("7"));assertEquals(result.get("invoicedQuantity"),new BigDecimal("6"));assertEquals(result.get("potentialImpact"),new BigDecimal("75.00"));
    assertEquals(result.get("currency"),"EUR");assertEquals(result.get("completedVisits"),7);
    assertEquals(arr(result.get("discrepancies")).size(),1);
    assertEquals(obj(arr(result.get("discrepancies")).get(0)).get("type"),"QUANTITY_MISMATCH");
    requireFailure(()->report(json,false,"application","EUR"),"unapproved rule");
    requireFailure(()->report(json,true,"flat fee","EUR"),"unsupported billing rule");
    String partial=json.replace("\"hasNextPage\":false", "\"hasNextPage\":true");
    requireFailure(()->report(partial,true,"application","EUR"),"partial pages");
    String errors=json.replaceFirst("\\\"data\\\"\\s*:","\"errors\":[{\"message\":\"denied\"}],\"data\":");
    requireFailure(()->report(errors,true,"application","EUR"),"GraphQL errors");
    String duplicate=json.replace("\"quantity\":6","\"quantity\":7");
    assertEquals(report(duplicate,true,"application","EUR").get("potentialImpact"),new BigDecimal("0.00"));
    System.out.println("PASS: 8 numeric/structural assertions and 4 fail-closed scenarios with uploaded Jobber fixture");
  }
  interface Checked {void run()throws Exception;}
  static void requireFailure(Checked work,String label)throws Exception{try{work.run();throw new AssertionError("Expected to reject "+label);}catch(IllegalArgumentException expected){System.out.println("PASS rejected "+label+": "+expected.getMessage());}}
  static void assertEquals(Object a,Object b){if(!Objects.equals(a,b))throw new AssertionError("Expected "+b+" got "+a);}
  /** Strict minimal JSON parser/serializer used only for this dependency-free test harness. */
  static final class Json {
    final String s;int i;Json(String s){this.s=s;}
    Object parse(){Object v=value();ws();if(i!=s.length())throw new IllegalArgumentException("Trailing JSON");return v;}
    void ws(){while(i<s.length()&&Character.isWhitespace(s.charAt(i)))i++;}
    char next(){if(i>=s.length())throw new IllegalArgumentException("Malformed JSON");return s.charAt(i++);}
    Object value(){ws();char c=next();switch(c){case '{':{Map<String,Object> m=new LinkedHashMap<>();ws();if(peek('}')){i++;return m;}do{ws();if(next()!='"')throw new IllegalArgumentException("Expected JSON string key");String k=quoted();ws();if(next()!=':')throw new IllegalArgumentException("Expected colon");m.put(k,value());ws();char end=next();if(end=='}')return m;if(end!=',')throw new IllegalArgumentException("Expected comma");}while(true);}case '[':{List<Object> out=new ArrayList<>();ws();if(peek(']')){i++;return out;}do{out.add(value());ws();char end=next();if(end==']')return out;if(end!=',')throw new IllegalArgumentException("Expected comma");}while(true);}case '"':return quoted();case 't':literal("rue");return true;case 'f':literal("alse");return false;case 'n':literal("ull");return null;default:if(c=='-'||Character.isDigit(c)){i--;return number();}throw new IllegalArgumentException("Invalid JSON token");}}
    boolean peek(char c){return i<s.length()&&s.charAt(i)==c;}
    void literal(String expected){for(int j=0;j<expected.length();j++)if(next()!=expected.charAt(j))throw new IllegalArgumentException("Invalid JSON literal");}
    BigDecimal number(){int start=i;while(i<s.length()&&"-+0123456789.eE".indexOf(s.charAt(i))>=0)i++;return new BigDecimal(s.substring(start,i));}
    String quoted(){StringBuilder b=new StringBuilder();while(true){char c=next();if(c=='"')return b.toString();if(c=='\\'){char e=next();switch(e){case 'n':b.append('\n');break;case 'r':b.append('\r');break;case 't':b.append('\t');break;case 'b':b.append('\b');break;case 'f':b.append('\f');break;case 'u':b.append((char)Integer.parseInt(s.substring(i,i+4),16));i+=4;break;default:b.append(e);}}else{if(c<32)throw new IllegalArgumentException("Invalid control char");b.append(c);}}}
    static String stringify(Object v){if(v==null)return"null";if(v instanceof Number || v instanceof Boolean)return String.valueOf(v);if(v instanceof Map<?,?> m){List<String> entries=new ArrayList<>();m.forEach((k,x)->entries.add(stringify(String.valueOf(k))+":"+stringify(x)));return"{"+String.join(",",entries)+"}";}if(v instanceof List<?> list){List<String> entries=new ArrayList<>();list.forEach(x->entries.add(stringify(x)));return"["+String.join(",",entries)+"]";}String s=String.valueOf(v);return"\""+s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t")+"\"";}
  }
}

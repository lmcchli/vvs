/*
 * MM7ServerServlet.java
 *
 * Created on March 26, 2004, 9:53 AM
 */
import java.util.*;
import java.io.*;
import javax.servlet.*;
import jakarta.servlet.http.*;
/**
 *
 * @author  ermmaha
 * @version
 */
public class MM7ServerServlet extends HttpServlet {
    
    static String _submitResponse;
    static String _faultResponse;
    static String _lastRequest = "";
    
    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if(_submitResponse == null) {
            _submitResponse = loadResponseFile("/apps/mws/content/servlets/Submit.RES");
        }
        if(_faultResponse == null) {
            _faultResponse = loadResponseFile("/apps/mws/content/servlets/FAULT_SPEC.RES");
        }
    }
    
    public void destroy() {
    }
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        if(request.getParameter("view") != null) {
            displayMM7Requests(request, response);
            return;
        }
        
        readMM7Request(request);
        
        response.setContentType("text/xml");
        //response.setStatus(403);
        
        //byte[] toSend = _faultResponse.getBytes();
        byte[] toSend = _submitResponse.getBytes();
        response.setContentLength(toSend.length);
        
        ServletOutputStream out = response.getOutputStream();
        out.write(toSend, 0, toSend.length);
        out.close();
    }
    
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        processRequest(request, response);
    }
    
    private boolean readMM7Request(HttpServletRequest request)
    throws ServletException, IOException {
        StringBuffer result = new StringBuffer();
        BufferedReader in = request.getReader();
        String line = "";
        while((line = in.readLine()) != null) {
            result.append(line);
            result.append("\n");
        }
        _lastRequest = result.toString();
        return true;
    }
    
    private void displayMM7Requests(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<pre>");
        out.println(secureField(_lastRequest));
        out.println("</pre>");
        out.println("</body>");
        out.println("</html>");
        out.close();
    }
    
    private String loadResponseFile(String filename) {
        StringBuffer strBuf = new StringBuffer();
        try {
            FileInputStream fis = new FileInputStream(filename);
            byte[] buf = new byte[4 * 1024];  // 4K buffer
            int bytesRead = 0;
            while ((bytesRead = fis.read(buf)) != -1) {
                strBuf.append(new String(buf, 0, bytesRead));
            }
            fis.close();
        }
        catch(Exception ex){ log("Error in loadResponseFile "+ex); }
        return strBuf.toString();
    }
    
    // Specializes some characters that can be harmful to the HTML environment, such as " and < >
    public static String secureField(String source) {
        if ( source == null || source.length() == 0 )
            return source;
        
        if((source.indexOf("\"") > -1) ||
        (source.indexOf("<") > -1) ||
        (source.indexOf(">") > -1) ||
        (source.indexOf("/") > -1) ||
        (source.indexOf("&") > -1)) {
            StringBuffer target = new StringBuffer(source.length() + 20);
            for (int i=0; i<source.length(); i++) {
                switch (source.charAt(i)) {
                    case 34 : // "
                        target.append("&");
                        target.append("#");
                        target.append("034;");
                        break;
                    case 38 : // &
                        target.append("&");
                        target.append("#");
                        target.append("038;");
                        break;
                    case 47 : // /
                        target.append("&");
                        target.append("#");
                        target.append("047;");
                        break;
                    case 60 : // <
                        target.append("&");
                        target.append("#");
                        target.append("060;");
                        break;
                    case 62 : // >
                        target.append("&");
                        target.append("#");
                        target.append("062;");
                        break;
                    default:
                        target.append(source.charAt(i));
                }
            }
            return target.toString();
        }
        else
            return source;
    }
}

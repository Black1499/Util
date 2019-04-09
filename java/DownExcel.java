
import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 下载
 */
@WebServlet("/DownloadXLS")
public class DownloadXLS extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String filename = request.getParameter("filename");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        // 写入bom头
        byte[] uft8bom={(byte)0xef,(byte)0xbb,(byte)0xbf};
    
        String name=URLEncoder.encode("月度收入报表.csv","utf-8"); 
        
        //修改http头部，设置输出为附件
        response.setHeader("Content-Disposition", "attachment;filename="+name);
        
        String result="日期,收入\r\n";
        
        for (int i = 1; i <=10; i++) {
            result+="2018-06-"+i+","+(i*10)+"万\r\n";
        }
        result=new String(result.getBytes(),"utf-8");
        //将字节流写入response中
        response.getOutputStream().write(uft8bom);  //写入头部解决乱码问题
        response.getOutputStream().write(result.getBytes());
        response.flushBuffer();
        response.getOutputStream().flush();
    }

}
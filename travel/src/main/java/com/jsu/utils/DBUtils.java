package main.java.com.jsu.utils;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import main.java.com.jsu.bean.scenic;
import org.apache.commons.beanutils.BeanUtils;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class DBUtils {

    public static Connection getConnection() throws Exception {
        //读取配置文件
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("config/db.properties");
        Properties properties = new Properties();
        properties.load(in);

        DataSource dataSource = DruidDataSourceFactory.createDataSource(properties);
        return dataSource.getConnection();
    }

    //类型参数，类型变量，类型是可以变化  Teacher Student
    public static <T> List<T> getList(Class<T> clazz, String sql, Object...args) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<T> userList = null;
        try {
            conn = DBUtils.getConnection();
            //String sql = "select id,username,birthday from users where id = ?";
            ps = conn.prepareStatement(sql);

            if (args!=null && args.length>0){
                for (int i=0;i<args.length;i++){
                    ps.setObject(i+1,args[i]);
                }
            }

            rs = ps.executeQuery();

            //获取结果集元数据
            ResultSetMetaData rsmd = rs.getMetaData();
            //获取当前结果集的列数
            int colnum = rsmd.getColumnCount();

            userList = new ArrayList<>();

            while (rs.next()){

                //key存放列名 ，value存放列值，for循环完成之后，rowmap存放了一条记录
                Map<String,Object> rowMap = new HashMap<String,Object>();
                for (int i = 1; i<=colnum;i++){
                    String columnName = rsmd.getColumnLabel(i);
                    Object columnValue = rs.getObject(columnName);
                    //判断查询出来的类的类型，如果是java.sql.Date转成java.util.Date
                    if (columnValue instanceof java.sql.Date){
                        java.sql.Date date = (java.sql.Date)columnValue;
                        columnValue = new Date(date.getTime());
//                        System.out.println("内部："+ columnName);
                    }
                    rowMap.put(columnName,columnValue);
                }
                //创建一个User对象，给这个User对象属性赋值；
                T bean = clazz.newInstance();
                // User user = new User();
                //clazz   User.class字节码文件
                // Class<User> clazz = User.class;

                //根据列名，id  == 给User对象 id属性赋值的方法名，setId，“set”+Id = setId，调用setId方法给user对象赋值，map中的value

                //循环rowMap给User所有属性赋值  entry {key,value} [{"id":1},{username:"king"},{"birthday":"2020-06-6"}]
                for(Map.Entry<String,Object> entry :rowMap.entrySet()){

                    String propertyName = entry.getKey();
                    Object propertyValue = entry.getValue();
                    BeanUtils.setProperty(bean,propertyName,propertyValue);
                }

                userList.add(bean);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            close(conn, ps, rs);
        }
        return userList;
    }

    public static void close(Connection conn, Statement ps, ResultSet rs) {
        if (rs !=null){
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (ps !=null){
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn !=null){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 保存对象的方法
    public static boolean save(String sql, Object...args) {
        Connection conn = null;
        PreparedStatement ps = null;
        Integer count = null;

        try {
            conn = DBUtils.getConnection();
            ps = conn.prepareStatement(sql);

            if (args!=null && args.length>0){
                for (int i=0;i<args.length;i++){
                    ps.setObject(i+1,args[i]);
                }
            }

            //返回更新的记录数
            count = ps.executeUpdate();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            close(conn, ps, null);
        }
        return count!=null&&count>0?true:false;
    }

    //类型参数，类型变量，类型是可以变化  Teacher Student
    public static <T> T getSingleObj(Class<T> clazz, String sql, Object...args) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        T bean = null;
        try {
            conn = DBUtils.getConnection();
            //String sql = "select id,username,birthday from users where id = ?";
            ps = conn.prepareStatement(sql);

            if (args!=null && args.length>0){
                for (int i=0;i<args.length;i++){
                    ps.setObject(i+1,args[i]);
                }
            }

            rs = ps.executeQuery();

            //获取结果集元数据
            ResultSetMetaData rsmd = rs.getMetaData();
            //获取当前结果集的列数
            int colnum = rsmd.getColumnCount();

            while (rs.next()){

                //key存放列名 ，value存放列值，for循环完成之后，rowmap存放了一条记录
                Map<String,Object> rowMap = new HashMap<String,Object>();
                for (int i = 1; i<=colnum;i++){
                    String columnName = rsmd.getColumnLabel(i);
                    Object columnValue = rs.getObject(columnName);
                    //判断查询出来的类的类型，如果时java.sql.Date转成java.util.Date
                    if (columnValue instanceof java.sql.Date){
                        java.sql.Date date = (java.sql.Date)columnValue;
                        columnValue = new java.util.Date(date.getTime());
                    }
                    rowMap.put(columnName,columnValue);
                }
                //创建一个User对象，给这个User对象属性赋值；
                bean = clazz.newInstance();
                //循环rowMap给User所有属性赋值  entry {key,value} [{"id":1},{username:"king"},{"birthday":"2020-06-6"}]
                for(Map.Entry<String,Object> entry :rowMap.entrySet()){

                    String propertyName = entry.getKey();
                    Object propertyValue = entry.getValue();
//                    System.out.println(propertyName+":"+propertyValue);
                    BeanUtils.setProperty(bean,propertyName,propertyValue);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            close(conn, ps, rs);
        }
        return bean;
    }

    //查询所有景点信息
    public static List<scenic> LL(String sql) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<scenic> list = new ArrayList<scenic>();
        try {
            conn = DBUtils.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                scenic s = new scenic();
                s.setId(rs.getInt(1));
                s.setScenic_name(rs.getString(2));
                s.setPlace(rs.getString(3));
                s.setPhoto(rs.getString(4));
                s.setShow(rs.getString(5));
                s.setPrice(rs.getDouble(6));
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(conn, ps, rs);
        }
        return list;
    }

    //查询记录数量
    public static Integer getCount(String sql, Object...args) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Integer count = null;

        try {
            conn = DBUtils.getConnection();
            //String sql = "select id,username,birthday from users where id = ?";
            ps = conn.prepareStatement(sql);


            if (args!=null && args.length>0){
                for (int i=0;i<args.length;i++){
                    ps.setObject(i+1,args[i]);
                }
            }

            rs = ps.executeQuery();

            while (rs.next()){

                count = rs.getInt(1);

            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            close(conn, ps, rs);
        }
        return count;
    }

    //更新操作
    public static boolean update(String sql, Object...args) {
        Connection conn = null;
        PreparedStatement ps = null;
        Integer count = 0;

        try {
            conn = DBUtils.getConnection();
            ps = conn.prepareStatement(sql);

            if (args!=null && args.length>0){
                for (int i=0;i<args.length;i++){

                    //判断当前类型，是不是java.util.Date，转换成java.sql.Date
                    if (args[i] instanceof Date){
                        Date date = (Date)args[i];
                        //转换成java.sql.Date
                        args[i] = new java.sql.Date(date.getTime());
                    }
                    ps.setObject(i+1,args[i]);
                }
            }

            count = ps.executeUpdate();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            close(conn, ps, null);
        }
        return count>0?true:false;
    }

    //删除操作
    public static boolean delete(String sql, Object...args) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean flag = false;

        try {
            conn = DBUtils.getConnection();
            ps = conn.prepareStatement(sql);

            if (args!=null && args.length>0){
                for (int i=0;i<args.length;i++){

                    //判断当前类型，是不是java.util.Date，转换成java.sql.Date
                    if (args[i] instanceof java.util.Date){
                        java.util.Date date = (java.util.Date)args[i];
                        //转换成java.sql.Date
                        args[i] = new java.sql.Date(date.getTime());

                    }


                    ps.setObject(i+1,args[i]);
                }
            }

            flag = ps.execute();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            close(conn, ps, null);
        }
        return flag;
    }


    //更新操作
    public static Integer updateForPrimary(String sql, Object...args) {
        Connection conn = null;
        PreparedStatement ps = null;
        Integer primaryKey = null;
        ResultSet rs = null;

        try {
            conn = DBUtils.getConnection();
            ps = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);

            if (args!=null && args.length>0){
                for (int i=0;i<args.length;i++){

                    //判断当前类型，是不是java.util.Date，转换成java.sql.Date
                    if (args[i] instanceof Date){
                        Date date = (Date)args[i];
                        //转换成java.sql.Date
                        args[i] = new java.sql.Date(date.getTime());

                    }


                    ps.setObject(i+1,args[i]);
                }
            }

            ps.executeUpdate();

            ResultSet generatedKeys = ps .getGeneratedKeys();

            rs = ps.getGeneratedKeys();

            if (rs.next()){
                primaryKey =  rs.getInt(1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            close(conn, ps, rs);
        }
        return primaryKey;
    }

}

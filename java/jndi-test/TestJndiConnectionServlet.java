/**
 *
 *  Simple test case to verify JNDI connectivity.
 *
 *  @author Will McDonald <wmcdonald@gmail.com>
 *
 */
package com.admiral.uk.infrastructure.tests;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.Collection;
 
import javax.naming.Context;
import javax.naming.InitialContext;

import javax.servlet.ServletException;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.sql.DataSource;

@WebServlet(
  name = "TestJndiConnection",
  description = "Servlet that tests a JndiConnection",
  urlPatterns = "/*",
  initParams =
    {
      @WebInitParam(name = "query", value = "PREPROCESSED_TEST_QUERY")
    })
public class TestJndiConnectionServlet extends HttpServlet
{
  /**
   * Test query parameter name - could be passed as a HTTP parameter, but might be awkward to format.
   */
  private static final String QUERY_PARAMETER = "query";

  /**
   * JNDI name parameter name.
   */
  private static final String JNDI_NAME_PARAMETER = "jndiName";

  /**
   * Database type query parameter name.
   */
//  private static final String TYPE_NAME_PARAMETER = "type";

  /**
   * AS400 database type.
   */
//  private static final String AS400_TYPE = "as400";

  /**
   * HTML content type.
   */
  private static final String HTML_CONTENT_TYPE = "text/html";

  /**
   * HTML prefix.
   */
  private static final String HTML_PREFIX = "<html><head><title>Infrastructure Test</title></head><body><h1>";

  /**
   * HTML suffix.
   */
  private static final String HTML_SUFFIX = "</h1></body></html>";

  /**
   * JNDI context.
   */
  private static final String JNDI_CONTEXT = "java:";
//  private static final String JNDI_CONTEXT = "java:comp/env";

  /**
   * Default data source prefix.
   */
  private static final String DEFAULT_DATASOURCE_PREFIX = "/";

  /**
   * JDBC prefix.
   */
//  private static final String JDBC_PREFIX = "jdbc/";

  /**
   * Run the specified query on the specified connection.
   */
  private static void testConnection(final Connection aConnection, final String aQuery, final PrintWriter aWriter) throws Exception
  {
    System.out.println("Attempting to run " + aQuery + " on " + aConnection);

    try
    {
      final Statement theStatement = aConnection.createStatement();

      try
      {
        final ResultSet results = theStatement.executeQuery(aQuery);

        try
        {
          while (results.next())
          {
            aWriter.print("Result - " + results.getString(1));
          }
        }
        finally
        {
           try
           {
             results.close();
           }
           catch (final Exception e)
           {
             aWriter.print("Error - " + e.getMessage());
           }
        }
      }
      finally
      {
         try
         {
           theStatement.close();
         }
         catch (final Exception e)
         {
           aWriter.print("Error - " + e.getMessage());
         }
      }
    }
    finally
    {
       try
       {
         aConnection.close();
       }
       catch (final Exception e)
       {
         aWriter.print("Error - " + e.getMessage());
       }
    }
  }

  /**
   * Test the JNDI connection in response to a GET request.
   */
  @Override
  protected void doGet(final HttpServletRequest aRequest,
                       final HttpServletResponse aResponse)
   throws ServletException, IOException
  {
    aResponse.setContentType(HTML_CONTENT_TYPE);

    final PrintWriter writer = aResponse.getWriter();

    writer.print(HTML_PREFIX);

    final String jndiName = aRequest.getParameter(JNDI_NAME_PARAMETER);

    String response = "";

    if ((null == jndiName) || jndiName.equals(""))
    {
      writer.print("Error - Must specify a JNDI name as a HTTP jndiName parameter");
    }
    else
    {
//      final String type = aRequest.getParameter(TYPE_NAME_PARAMETER);

      String dsPrefix = DEFAULT_DATASOURCE_PREFIX;

//      if ((null == type) || type.equals(""))
//      {
//        writer.print("Error - Must specify a database type as a HTTP type parameter");
//      }
//      else
//      {
//        if (type.equalsIgnoreCase(AS400_TYPE))
//        {
//          dsPrefix = JDBC_PREFIX;
//        }

        try
        {
          final Context initial = new InitialContext();
          final Context env = (Context)initial.lookup(JNDI_CONTEXT);
          final DataSource ds = (DataSource)env.lookup(dsPrefix + jndiName);
          final Connection jndiConnection = ds.getConnection();
          final String query = getInitParameter(QUERY_PARAMETER);

          testConnection(jndiConnection, query, writer);
        }
        catch (final Exception e)
        {
          writer.print("Error - " + e.getMessage());
        }
//      }
    }

    writer.println(HTML_PREFIX);
    writer.close();
  }
}

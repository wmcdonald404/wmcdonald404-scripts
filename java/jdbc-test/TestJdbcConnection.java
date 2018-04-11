/**
 *
 * Simple test case to verify JT400 or Oracle Thin JDBC connectivity.
 *
 * @author Will McDonald <wmcdonald@gmail.com>
 *
 */
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
 
class TestJdbcConnection
{
  /**
   * Run the specified query on the specified connection.
   */
  private static void testConnection(final Connection aConnection, final String aQuery) throws Exception
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
            System.out.println (results.getString(1));
          }
        }
        finally
        {
           try
           {
             results.close();
           }
           catch (final Exception ignore) {}
        }
      }
      finally
      {
         try
         {
           theStatement.close();
         }
         catch (final Exception ignore) {}
      }
    }
    finally
    {
       try
       {
         aConnection.close();
       }
       catch (final Exception ignore) {}
    }
  }

  /**
   * Print the help message.
   */
  private static void printHelp(final Options aOptions)
  {
    System.out.println("java TestJndiConnection");

    final Collection<Option> theOptions = aOptions.getOptions();

    for (final Option theOption : theOptions)
    {
      System.out.println(theOption.getOpt() + " " + theOption.getLongOpt() + " " + theOption.getDescription());
    }
  }

  /**
   * Print the specified error message and the help message.
   */
  private static void printErrorAndHelp(final String aErrorMessage, final Options aOptions) throws Exception
  {
    System.out.println("Error: " + aErrorMessage);

    printHelp(aOptions);

    throw new IllegalArgumentException(aErrorMessage);
  }

  /**
   * Main method.
   */
  public static void main(final String[] args) throws Exception
  {
    final CommandLineParser parser = new BasicParser();
    final Options theOptions = new Options();

    theOptions.addOption("h", "help", false, "Print this usage information");
    theOptions.addOption("n", "hostname", true, "Hostname of the database server");
    theOptions.addOption("o", "port", true, "TNS Listener port of the database server");
    theOptions.addOption("s", "sid", true, "Database SID or service name to connect to");
    theOptions.addOption("u", "username", true, "Username/schema to connect to" );
    theOptions.addOption("p", "password", true, "Password to connect with");
    theOptions.addOption("t", "type", true, "Type of connection - oracle (the default) or as400");
    theOptions.addOption("q", "query", true, "Test query statement");

    // Parse the program arguments.
    final CommandLine cli = parser.parse(theOptions, args);
    final Option[] parsedOptions = cli.getOptions();

    System.out.println("Running with options:");

    for (final Option theOption : parsedOptions)
    {
      System.out.println(theOption.getOpt() + " " + theOption.getValue());
    }

    if (cli.hasOption('h'))
    {
      printHelp(theOptions);
      System.exit(0);
    }

    String query = "";

    if (cli.hasOption('q'))
    {
      query = cli.getOptionValue('q');
    }
    else
    {
      printErrorAndHelp("Must specify a query", theOptions);
    }

    String hostname = "";
    String port = "";
    String sid = "";
    String username = "";
    String password = "";

    if (cli.hasOption('n'))
    {
      hostname = cli.getOptionValue('n');
    }
    else
    {
      printErrorAndHelp("Missing hostname", theOptions);
    }

    if (cli.hasOption('o'))
    {
      port = cli.getOptionValue('o');
    }
    else
    {
      printErrorAndHelp("Missing port", theOptions);
    }

    if (cli.hasOption('s'))
    {
      sid = cli.getOptionValue('s');
    }
    else
    {
      printErrorAndHelp("Missing SID/service name", theOptions);
    }

    if (cli.hasOption('u'))
    {
      username = cli.getOptionValue('u');
    }
    else
    {
      printErrorAndHelp("Missing username", theOptions);
    }

    if (cli.hasOption('p'))
    {
      password = cli.getOptionValue('p');
    }
    else
    {
      printErrorAndHelp("Missing password", theOptions);
    }

    String type = "oracle";

    if (cli.hasOption('t'))
    {
      type = cli.getOptionValue('t');
    }

    String typeConnection = "";

    // TODO Implement as an enum.
    switch (type.toLowerCase())
    {
      case "oracle":
        typeConnection = "oracle:thin:@" + hostname + ":" + port + ":" + sid; // jdbc:oracle:thin:@hostname:port:sid
        break;
      case "as400":
          typeConnection = "as400://" + hostname + ":" + port + "/" + sid; // jdbc:as400://nonprod.admiral.uk:50000/nonprod
        break;
      default:
        printErrorAndHelp("Invalid type", theOptions);
    }

    final String jdbcUrl = "jdbc:" + typeConnection;
    final Connection jdbcConnection = DriverManager.getConnection(jdbcUrl, username, password);

    testConnection(jdbcConnection, query);
  }
}

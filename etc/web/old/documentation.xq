import module namespace hp="http://www.basex.org/" at "hp.xqm";

declare variable $file := "documentation";
declare variable $title := "Documentation";
declare variable $cont :=
  <div id="main">
  <h1>Documentation</h1>

  <h2>Quick Start</h2>
  <p>
  BaseX comes as an executable <code>.jar</code> (Java Archive), which
  automatically determines the correct system settings (i.e. paths and
  environment variables) for BaseX.
  </p>

  <p>By default, the graphical interface of BaseX is launched.<br/>
  To manually start BaseX you have (at least) the following options:

  <ul>
    <li>Start the GUI, granting more memory to the virtual machine:<br/>
      <code>java -Xmx512m -jar BaseX.jar</code>
    </li>
    <li>Start the console version:<br/>
      <code>java -cp BaseX.jar org.basex.BaseX</code>
    </li>
  </ul>

  Please ensure to use <a href="http://java.sun.com/javase/downloads">Java 5</a> or later.</p>

  <h2>Commands</h2>
  <p>
  Typing <code>help</code> at the command prompt gives you a
  list of the <a href="{ hp:link("commands") }">available commands</a>.
  </p>

  <h2>FAQ</h2>
  <p>
  Currently we are working on a <a href="{ hp:link("faq") }">FAQ</a> section.
  </p>

  <h2>Code &amp; Sources</h2>
  <p>
  Two examples for
  <a href='{ hp:link("xpath") }'>XPath</a> and
  <a href='{ hp:link("xquery") }'>XQuery</a> might help you to integrate BaseX
  in your own project.
  Here you find the <a href="javadoc">JavaDocs</a> of the source code.
  </p>

  </div>;

hp:print($title, $file, $cont)

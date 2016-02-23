<?php
    $results = json_decode(file_get_contents('http://gw127.iu.xsede.org:8000/query-api/select?q=sddslfnlsdf'), true);
    if(!isset($results) || empty($results)){
        $results = array();
    }
?>

<html>
    <head>
        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
              integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous">

        <!-- Optional theme -->
        <link rel="stylesheet" href="css/index.css">

    </head>
    <body>

        <nav class="navbar navbar-inverse navbar-fixed-top">
            <div class="container">
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar"
                            aria-expanded="false" aria-controls="navbar">
                        <span class="sr-only">Toggle navigation</span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
                    <a class="navbar-brand" href="./index.php">GridChem Data Catalog</a>
                </div>
                <div id="navbar" class="collapse navbar-collapse">
                    <ul class="nav navbar-nav">
                        <li><a href="./search.php">Search</a></li>
                    </ul>
                    <ul class="nav navbar-nav pull-right">
                        <li><a href="./login.php">Login</a></li>
                    </ul>
                </div>
            </div>
        </nav>

        <div class="container">

            <div class="center-content">
                <h1>Welcome to GridChem Data Catalog</h1>
                <div style="text-align: justify;text-justify: inter-word">
                    <p class="lead">GridChem Data Catalog provides a sleek web interface for you to browse and
                        search through your GridChem data. Currently the system can index outputs of several computational
                        chemistry applications including Gaussian, Gamess, Molpro and NWChem. Also it allows to publish your
                        data into research data publishing systems, do browser based visualization of molecular structure and
                        properties and to run complex search queries to filter the data. So now you don't need to download all
                        the data into your local machine after running a HPC application but select only the interesting
                        data based on the results of configured post processing steps in the system.
                    </p>
                    <p style="color: red">N.B: This data is automatically extracted using a set of configured parsers and may contain errors.
                        Please report any issues in the <a href="https://issues.apache.org/jira/browse/AIRAVATA/?
                    selectedTab=com.atlassian.jira.jira-projects-plugin:summary-panel" target="_blank">issue tracker</a></p>
                </div>

            </div>

        </div><!-- /.container -->

        <!--JQuery MinJS-->
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
        <!-- Latest compiled and minified JavaScript -->
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"
                integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS" crossorigin="anonymous"></script>
    </body>
</html>

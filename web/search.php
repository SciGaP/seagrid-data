<?php
    include 'config.php';
    //FIXME Time should be shown in locally
    date_default_timezone_set('America/Indianapolis');

    function convert_date($hit)
    {
        $hit[0] = substr($hit[0],1,-1);
        return intval(strtotime($hit[0])*1000);
    }

    session_start();
    if(!isset($_SESSION['username'])){
        $home_url = 'http://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/index.php';
        header('Location: ' . $home_url);
    }

    $q = "";
    $pageNo = 1;
    $pageSize = 50;
    if(isset($_POST['query'])){
        $q = $_POST['query'];
        //converting time to unix timestamp
        $q = preg_replace_callback('~("\\d{4}/\\d{2}/\\d{2}")~', convert_date, $q);
    }
    if(!isset($_POST['search']) && isset($_POST['pageNo'])){
        $pageNo = $_POST['pageNo'];
    }
    if(!isset($_POST['search']) && isset($_POST['pageSize'])){
        $pageSize = $_POST['pageSize'];
    }
    if(!isset($_POST['search']) && isset($_POST['next'])){
        $pageNo = $pageNo + 1;
    }
    if(!isset($_POST['search']) && isset($_POST['previous'])){
        $pageNo = $pageNo - 1;
    }
    $offset = ($pageNo -1) * $pageSize;
    $username = $_SESSION['username'];
    $results = json_decode(file_get_contents('http://' . SERVER_HOST . ':8000/query-api/select?username='.$username.'&q='. urlencode($q)
        .'&limit=' . $pageSize . '&offset=' . $offset), true);
    if(!isset($results) || empty($results)){
        $results = array();
    }
    $id = $_GET['id'];
        if(isset($id)){
        $record = json_decode(file_get_contents('http://' . SERVER_HOST . ':8000/query-api/get?username=' .  $_SESSION['username'] . '&id=' . $id), true);
    } else {
     ////   //echo 'Id not set !!!';
        echo '';
    }
?>

<html>
    <head>
        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
              integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous">

        <!-- Optional theme -->
        <link rel="stylesheet" href="assets/css/search.css">
        <!-- Query builder -->
        <link rel="stylesheet" href="assets/css/query-builder.default.min.css">
        <link rel="stylesheet" href="assets/css/bootstrap-datepicker.css">

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
                    <a class="navbar-brand" href="./index.php">SEAGrid Data Catalog</a>
                </div>
                <div id="navbar" class="collapse navbar-collapse">
                    <ul class="nav navbar-nav">
                        <li class="active"><a href="./search.php">Search</a></li>
                        <li><a href="./browse.php">Directory Browser</a></li>
                    </ul>
                    <ul class="nav navbar-nav pull-right">
                        <li class="dropdown">
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown"><?php echo $_SESSION['username'] ?><span class="caret"></span></a>
                            <ul class="dropdown-menu" role="menu">
                                <li><a href="./logout.php"><span class="glyphicon glyphicon-log-out"></span> Log out</a></li>
                            </ul>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>

        <div class="container">
            <div class="center-content">
                <div id="builder"></div>
                <div class="btn-group pull-right">
                    <button id="btn-reset" class="btn btn-warning reset">Reset</button>
                    <button id="btn-search" class="btn btn-primary parse-json">Search</button>
                    <div class="btn-group">
                        <button id="pageSizeButton" type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown">
                            Page Size <?php echo $pageSize;?>
                        </button>
                        <ul class="dropdown-menu" role="menu">
                            <li><a class="dropdown-item" href="#" onclick="setPageSize(10)">10</a></li>
                            <li><a class="dropdown-item" href="#" onclick="setPageSize(50)">50</a></li>
                            <li><a class="dropdown-item" href="#" onclick="setPageSize(100)">100</a></li>
                            <li><a class="dropdown-item" href="#" onclick="setPageSize(200)">200</a></li>
                        </ul>
                    </div>
                </div>
                <form id="searchForm" action="./search.php" method="post">
                    <div class="form-group search-text-block">
                        <input id="query" name="query" type="hidden" value='<?php if (isset($_POST['query'])) echo $_POST['query'] ?>'>
                        <input type="hidden" name="pageNo" id="pageNo" value=<?php echo $pageNo; ?>>
                        <input type="hidden" name="pageSize" id="pageSize" value=<?php echo $pageSize; ?>>
                    </div>
                    <br>
                    <hr>
                    <table class="table table-bordered" id="data-table">
                        <tr>
                            <th class="col-md-2">Experiment Name</th>
                            <th class="col-md-2">Owner Name</th>
                            <th class="col-md-2">Package</th>
                            <th class="col-md-2">Formula</th>
                            <!-- th class="col-md-2">Indexed Time</th -->
                            <th class="col-md-2">Finished Time</th>
                            <th class="col-md-2">Basis Set</th>
                            <th class="col-md-2">Number of Basis Functions</th>
                            <th class="col-md-2">Energy</th>
                            <?php foreach ($results as $result): ?>
                        <tr>
                            <td><a href="./summary.php?id=<?php echo $result['Id']?>" target="_blank">
                                    <?php echo substr($result['ExperimentName'], 0, -10)?></a></td>
                            <td><?php echo $result['Username']?></td>
                            <td><?php echo $result['Calculation']['Package']?></td>
                            <td><?php echo $result['Molecule']['Formula']?></td>
                            <td>
                                <?php
                                    //$date = new DateTime();
                                     echo $result['ExecutionEnvironment']['FinTime'];
                                     //echo $record['ExecutionEnvironment']['FinTime'];
                                    //$date->setTimestamp($record['ExecutionEnvironment']['FinTime']/1000);
                                    //$date->setTimestamp($result['ExecutionEnvironment.FinTime']/1000);
                                    //$date->setTimestamp($result['IndexedTime']/1000);
                                    //echo $date->format('d-M-Y')
                                    //echo $date->format('Y-m-d')
                                    //echo $date->format('Y-m-d H:i:s')
                                ?>
                            </td>
                            <td><?php echo $result['Calculation']['Basis']?></td>
                            <td><?php echo $result['Calculation']['NBasis']?></td>
                            <td><?php echo $result['CalculatedProperties']['Energy']?></td>
                        </tr>
                        <?php endforeach;?>
                        </tr>
                    </table>
                    <button id="export" data-export="export">Export to CSV</button>
                    <div class="pull-right btn-toolbar" style="padding-bottom: 5px">
                        <?php
                            if (isset($pageNo) && $pageNo != 1) {
                                echo '<input class="btn btn-primary btn-xs" type="submit" style="cursor: pointer" name="previous" value="previous"/>';
                            }
                            if (sizeof($results) > 0 && sizeof($results) == $pageSize) {
                                echo '<input class="btn btn-primary btn-xs" type="submit" style="cursor: pointer" name="next" value="next"/>';
                            }
                        ?>
                    </div>
                </form>
            </div>

        </div><!-- /.container -->

        <!--JQuery MinJS-->
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
        <!-- Latest compiled and minified JavaScript -->
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"
                integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS" crossorigin="anonymous"></script>
        <!-- Query builder-->
        <script src="assets/js/query-builder.standalone.js"></script>
        <script src="assets/js/moment.min.js"></script>
        <script src="assets/js/bootstrap-datepicker.js"></script>

        <script>
            $( document ).ready(function() {
                var rules_basic = {
                    condition: 'AND',
                    rules: [{
                        id: 'ExperimentName',
                        operator: 'contains',
                        value: ''
                    }]
                };

                $('#builder').queryBuilder({
                    plugins: ['bt-tooltip-errors'],

                    filters: [{
                        id: 'ExperimentName',
                        label: 'Experiment Name',
                        type: 'string',
                        operators: ['contains']
                    }, {
                        id: 'ProjectName',
                        label: 'Project Name',
                        type: 'string',
                        operators: ['contains']
                    }, {
                        id: 'Calculation.Package',
                        label: 'Package',
                        type: 'string',
                        operators: ['contains']
                    }, {
                        id: 'Molecule.Formula',
                        label: 'Formula',
                        type: 'string',
                        operators: ['contains', 'equal']
                    }, {
                        id: 'Identifiers.InChI',
                        label: 'InChI',
                        type: 'string',
                        operators: ['contains']
                    }, {
                        id: 'Identifiers.SMILES',
                        label: 'SMILES',
                        type: 'string',
                        operators: ['contains']
                    },{
                        id: 'Calculation.CalcType',
                        label: 'Calculation Type',
                        type: 'string',
                        operators: ['contains']
                    },{
                        id: 'Calculation.Methods',
                        label: 'Calculation Methods',
                        type: 'string',
                        operators: ['contains']
                    },{
                        id: 'Calculation.Basis',
                        label: 'Basis Sets',
                        type: 'string',
                        operators: ['contains']
                    },{
                        id: 'Molecule.NAtom',
                        label: 'Number of Atoms',
                        type: 'integer',
                        operators: ['equal', 'less', 'greater']
                    },{
                        id: 'ExecutionEnvironment.ActualJobRunTime',
                        label: 'Actual Job Run Time',
                        type: 'integer',
                        operators: ['equal', 'less', 'greater']
                    }, {
                        id: 'ExecutionEnvironment.FinTime',
                        label: 'Finished Time',
                        type: 'string',
                        operators: ['contains']
                    }, {
                        id: 'IndexedTime',
                        label: 'Indexed Time',
                        type: 'date',
                        operators: ['between'],
                        validation: {
                            format: 'YYYY/MM/DD'
                        },
                        plugin: 'datepicker',
                        plugin_config: {
                            format: 'yyyy/mm/dd',
                            todayBtn: 'linked',
                            todayHighlight: true,
                            autoclose: true
                        }
                    }],

                    rules: rules_basic
                });

                $('#btn-reset').on('click', function() {
                    $('#builder').queryBuilder('reset');
                    $('#builder').queryBuilder('setRules', rules_basic);
                });

                $('#btn-search').on('click', function() {
                    var result = $('#builder').queryBuilder('getMongo');
                    if(JSON.stringify(result, null, 2) != "{}"){
                        $('#query').val(JSON.stringify(result, null, 2));
                        $('#pageNo').val(1);
                        $('form#searchForm').submit();
                    }
                });

                if($('#query').val() != ""){
                    var result = $('#query').val();
                    $('#builder').queryBuilder('setRulesFromMongo', $.parseJSON(result));
                }

                $("#export").click(function(){
                    $("#data-table").tableToCSV();
                });
            });

            function setPageSize(size) {
                var result = $('#builder').queryBuilder('getMongo');
                if(JSON.stringify(result, null, 2) != "{}"){
                    $('#query').val(JSON.stringify(result, null, 2));
                    $('#pageNo').val(1);
                    $('#pageSize').val(size);
                    $('form#searchForm').submit();
                }
            }

            jQuery.fn.tableToCSV = function() {

                var clean_text = function(text){
                    text = text.replace(/"/g, '""');
                    return '"'+text.trim()+'"';
                };

                $(this).each(function(){
                    var table = $(this);
                    var caption = $(this).find('caption').text();
                    var title = [];
                    var rows = [];

                    $(this).find('tr').each(function(){
                        var data = [];
                        $(this).find('th').each(function(){
                            var text = clean_text($(this).text());
                            title.push(text);
                        });
                        $(this).find('td').each(function(){
                            var text = clean_text($(this).text());
                            data.push(text);
                        });
                        data = data.join(",");
                        rows.push(data);
                    });
                    title = title.join(",");
                    rows = rows.join("\n");

                    var csv = title + rows;
                    var uri = 'data:text/csv;charset=utf-8,' + encodeURIComponent(csv);
                    var download_link = document.createElement('a');
                    download_link.href = uri;
                    var ts = new Date().getTime();
                    if(caption==""){
                        download_link.download = ts+".csv";
                    } else {
                        download_link.download = caption+"-"+ts+".csv";
                    }
                    document.body.appendChild(download_link);
                    download_link.click();
                    document.body.removeChild(download_link);
                });

            };
        </script>
    </body>
</html>

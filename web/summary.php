<?php
    session_start();
    if(!isset($_SESSION['username'])){
        $home_url = 'http://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/index.php';
        header('Location: ' . $home_url);
    }

    $id = $_GET['id'];
    if(isset($id)){
        $molecule = json_decode(file_get_contents(
            'http://gw127.iu.xsede.org:8000/query-api/get?id=' . $id), true);
    }else{
        echo 'Id not set !!!';
    }
?>

<?php if(isset($molecule)): ?>
<html>
    <head>
        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
              integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous">

        <!-- Optional theme -->
        <link rel="stylesheet" href="css/summary.css">


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
                        <li><a href="./logout.php">Logout</a></li>
                    </ul>
                </div>
            </div>
        </nav>

        <div class="container">
            <div class="center-content">
                <div style="color: red">&nbsp;&nbsp;&nbsp;N.B: This data is automatically extracted using set of configured parser
                    and may contain errors. Please report any issues in the <a href="https://issues.apache.org/jira/browse/AIRAVATA/?
                    selectedTab=com.atlassian.jira.jira-projects-plugin:summary-panel" target="_blank">issue tracker</a></div>
                <div class="col-md-8 text-centered">
                    <hr>
                    <table class="table table-bordered">
                        <tr><td><h4>Identifiers</h4></td><td></td></tr>
                        <?php if(isset($molecule['Identifiers']['InChI'])):?>
                        <tr>
                            <td>InChI</td>
                            <td><?php echo $molecule['Identifiers']['InChI']?></td>
                        </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['Identifiers']['InChIKey'])):?>
                            <tr>
                                <td>InChI Key</td>
                                <td><?php echo $molecule['Identifiers']['InChIKey']?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['Identifiers']['SMILES'])):?>
                            <tr>
                                <td>SMILES</td>
                                <td><?php echo $molecule['Identifiers']['SMILES']?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['Identifiers']['CanonicalSMILES'])):?>
                            <tr>
                                <td>Canonical SMILES</td>
                                <td><?php echo $molecule['Identifiers']['CanonicalSMILES']?></td>
                            </tr>
                        <?php endif; ?>

                        <tr><td><h4>Organization</h4></td><td></td></tr>
                        <?php if(isset($molecule['ExperimentName'])):?>
                        <tr>
                            <td>Experiment</td>
                            <td><?php echo $molecule['ExperimentName']?></td>
                        </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['ProjectName'])):?>
                            <tr>
                                <td>Project</td>
                                <td><?php echo $molecule['ProjectName']?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['Username'])):?>
                            <tr>
                                <td>Owner</td>
                                <td><?php echo $molecule['Username']?></td>
                            </tr>
                        <?php endif; ?>

                        <tr><td><h4>Calculation</h4></td><td></td></tr>
                        <?php if(isset($molecule['Calculation']['Package'])):?>
                        <tr>
                            <td>Package</td>
                            <td><?php echo $molecule['Calculation']['Package'] ?></td>
                        </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['Calculation']['CalcType'])):?>
                        <tr>
                            <td>Calculation Type</td>
                            <td><?php echo $molecule['Calculation']['CalcType'] ?></td>
                        </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['Calculation']['Basis'])):?>
                            <tr>
                                <td>Basis</td>
                                <td><?php echo $molecule['Calculation']['Basis'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['Calculation']['NBasis'])):?>
                            <tr>
                                <td>Number of Basis Functions</td>
                                <td><?php echo $molecule['Calculation']['NBasis'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['Calculation']['Keywords'])):?>
                            <tr>
                                <td>Keywords</td>
                                <td><?php echo $molecule['Calculation']['Keywords'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['Calculation']['JobStatus'])):?>
                            <tr>
                                <td>Job Status</td>
                                <td><?php echo $molecule['Calculation']['JobStatus'] ?></td>
                            </tr>
                        <?php endif; ?>

                        <tr><td><h4>Molecule</h4></td><td></td></tr>
                        <?php if(isset($molecule['Molecule']['Formula'])):?>
                            <tr>
                                <td>Formula</td>
                                <td><?php echo $molecule['Molecule']['Formula'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['Molecule']['NAtom'])):?>
                            <tr>
                                <td>Number of Atoms</td>
                                <td><?php echo $molecule['Molecule']['NAtom'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['Molecule']['NMo'])):?>
                            <tr>
                                <td>Molecular Mass</td>
                                <td><?php echo $molecule['Molecule']['NMo'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['Molecule']['ElecSym'])):?>
                            <tr>
                                <td>Electron Symmetry</td>
                                <td><?php echo $molecule['Molecule']['ElecSym'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['Molecule']['Multiplicity'])):?>
                            <tr>
                                <td>Multiplicity</td>
                                <td><?php echo $molecule['Molecule']['Multiplicity'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['Molecule']['Charge'])):?>
                            <tr>
                                <td>Charge</td>
                                <td><?php echo $molecule['Molecule']['Charge'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['Molecule']['OrbSym'])):?>
                            <tr>
                                <td>Orbital Symmetry</td>
                                <td><?php echo $molecule['Molecule']['OrbSym'] ?></td>
                            </tr>
                        <?php endif; ?>

                        <tr><td><h4>Calculated Properties</h4></td><td></td></tr>
                        <?php if(isset($molecule['CalculatedProperties']['Energy'])):?>
                            <tr>
                                <td>Energy</td>
                                <td><?php echo $molecule['CalculatedProperties']['Energy'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['CalculatedProperties']['Dipole'])):?>
                            <tr>
                                <td>Dipole</td>
                                <td><?php echo $molecule['CalculatedProperties']['Dipole'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['CalculatedProperties']['HF'])):?>
                            <tr>
                                <td>HF</td>
                                <td><?php echo $molecule['CalculatedProperties']['HF'] ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['CalculatedProperties']['Homos'])):?>
                            <tr>
                                <td>Homos</td>
                                <td><?php echo json_encode($molecule['CalculatedProperties']['Homos']); ?></td>
                            </tr>
                        <?php endif; ?>

                        <tr><td><h4>Execution Environment</h4></td></tr>
                        <?php if(isset($molecule['ExecutionEnvironment']['CalcBy'])):?>
                            <tr>
                                <td>Calculated By</td>
                                <td><?php echo $molecule['ExecutionEnvironment']['CalcBy']; ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['ExecutionEnvironment']['CalcMachine'])):?>
                            <tr>
                                <td>Calculated Machine</td>
                                <td><?php echo $molecule['ExecutionEnvironment']['CalcMachine']; ?></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['ExecutionEnvironment']['FinTime'])):?>
                            <tr>
                                <td>Finished Time</td>
                                <td><?php echo $molecule['ExecutionEnvironment']['FinTime']; ?></td>
                            </tr>
                        <?php endif; ?>

                        <tr><td><h4>Result Files</h4></td></tr>
                        <?php if(isset($molecule['Files']['GaussianInputFile'])):?>
                            <tr>
                                <td>Gaussian Input File</td>
                                <td><a href=./download.php?file=<?php echo $molecule['Files']['GaussianInputFile']; ?>>
                                    <?php echo basename($molecule['Files']['GaussianInputFile']); ?></a></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['Files']['GaussianOutputFile'])):?>
                            <tr>
                                <td>Gaussian Output File</td>
                                <td><a href=./download.php?file=<?php echo $molecule['Files']['GaussianOutputFile']; ?>>
                                        <?php echo basename($molecule['Files']['GaussianOutputFile']); ?></a></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['Files']['GaussianCheckpointFile'])):?>
                            <tr>
                                <td>Gaussian Checkpoint File</td>
                                <td><a href=./download.php?file=<?php echo $molecule['Files']['GaussianCheckpointFile']; ?>>
                                        <?php echo basename($molecule['Files']['GaussianCheckpointFile']); ?></a></td>
                            </tr>
                        <?php endif; ?>
                        <?php if(isset($molecule['Files']['GaussianFCheckpointFile'])):?>
                            <tr>
                                <td>Gaussian Formatted Checkpoint File</td>
                                <td><a href=./download.php?file=<?php echo $molecule['Files']['GaussianFCheckpointFile']; ?>>
                                        <?php echo basename($molecule['Files']['GaussianFCheckpointFile']); ?></a></td>
                            </tr>
                        <?php endif; ?>
                    </table>
                </div>
                <div class="col-md-4">
                    <hr>
                    <div id="glmol01" style="width: 300px; height: 300px; background-color: black;margin-left: 10%"></div>
                    <textarea id="glmol01_src" style="display: none;">
                        <?php var_dump($molecule['FinalMoleculeStructuralFormats']['SDF'])?>
                    </textarea>
                    <div class="text-centered">Molecular Structure</div>
                    <br><br>
                    <?php if(isset($molecule['CalculatedProperties']['EnergyDistribution'])):?>
                        <canvas id="energyDistribution" width="300" height="300" style="margin-left: 10%"></canvas>
                        <div class="text-centered">Energy vs Iteration</div>
                    <?php endif; ?>
                    <?php if(isset($molecule['CalculatedProperties']['MaximumGradientDistribution'])):?>
                        <br><br>
                        <canvas id="gradientDistribution" width="300" height="300" style="margin-left: 10%"></canvas>
                        <div class="text-centered">Gradient vs Iteration</div>
                    <?php endif; ?>
                </div>
            </div>

        </div><!-- /.container -->

        <!--JSMol-->
        <script src="./js/Three.js"></script>
        <script src="./js/GLmol.js"></script>
        <!--JQuery MinJS-->
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
        <!-- Latest compiled and minified JavaScript -->
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"
                integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS" crossorigin="anonymous"></script>
        <!--Charting library-->
        <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/1.0.2/Chart.min.js"></script>
    <script>
        $( document ).ready(function() {
            var glmol01 = new GLmol('glmol01', true);

            glmol01.defineRepresentation = function() {
                var all = this.getAllAtoms();
                var hetatm = this.removeSolvents(this.getHetatms(all));
                this.colorByAtom(all, {});
                this.colorByChain(all);
                var asu = new THREE.Object3D();
                this.drawBondsAsStick(asu, hetatm, this.cylinderRadius, this.cylinderRadius);
                this.drawBondsAsStick(asu, this.getResiduesById(this.getSidechains(this.getChain(all, ['A'])), [58, 87]),
                    this.cylinderRadius, this.cylinderRadius);
                this.drawBondsAsStick(asu, this.getResiduesById(this.getSidechains(this.getChain(all, ['B'])), [63, 92]),
                    this.cylinderRadius, this.cylinderRadius);
                this.drawCartoon(asu, all, this.curveWidth, this.thickness);
                this.drawSymmetryMates2(this.modelGroup, asu, this.protein.biomtMatrices);
                this.modelGroup.add(asu);
            };

            glmol01.loadMolecule();
        });
    </script>
        <script>
            $( document ).ready(function() {
                <?php if(isset($molecule['CalculatedProperties']['MaximumGradientDistribution'])):?>
                var gradientData = {
                    labels: <?php
                                if(sizeof($molecule['CalculatedProperties']['Iterations']) > 20){
                                    $step = sizeof($molecule['CalculatedProperties']['Iterations'])/20;
                                    echo "[" . $molecule['CalculatedProperties']['Iterations'][0];
                                    $i = $step;
                                    while($i < sizeof($molecule['CalculatedProperties']['Iterations'])){
                                        echo "," . $molecule['CalculatedProperties']['Iterations'][round($i)];
                                        $i = $i + $step;
                                    }
                                    echo "]";
                                }else{
                                    echo json_encode($molecule['CalculatedProperties']['Iterations']);
                                }
                            ?>,
                    datasets: [
                        {
                            label: "Maximum Gradient",
                            fillColor: "rgba(220,220,220,0.2)",
                            strokeColor: "rgba(220,220,220,1)",
                            pointColor: "rgba(220,220,220,1)",
                            pointStrokeColor: "#fff",
                            pointHighlightFill: "#fff",
                            pointHighlightStroke: "rgba(220,220,220,1)",
                            data: <?php
                                        if(sizeof($molecule['CalculatedProperties']['MaximumGradientDistribution']) > 20){
                                            $step = sizeof($molecule['CalculatedProperties']['MaximumGradientDistribution'])/20;
                                            echo "[" . $molecule['CalculatedProperties']['MaximumGradientDistribution'][0];
                                            $i = $step;
                                            while($i < sizeof($molecule['CalculatedProperties']['MaximumGradientDistribution'])){
                                                echo "," . $molecule['CalculatedProperties']['MaximumGradientDistribution'][round($i)];
                                                $i = $i + $step;
                                            }
                                            echo "]";
                                        }else{
                                            echo json_encode($molecule['CalculatedProperties']['MaximumGradientDistribution']);
                                        }
                                    ?>
                        },
                        {
                            label: "RMS Gradient",
                            fillColor: "rgba(151,187,205,0.2)",
                            strokeColor: "rgba(151,187,205,1)",
                            pointColor: "rgba(151,187,205,1)",
                            pointStrokeColor: "#fff",
                            pointHighlightFill: "#fff",
                            pointHighlightStroke: "rgba(151,187,205,1)",
                            data: <?php
                                        if(sizeof($molecule['CalculatedProperties']['RMSGradientDistribution']) > 20){
                                            $step = sizeof($molecule['CalculatedProperties']['RMSGradientDistribution'])/20;
                                            echo "[" . $molecule['CalculatedProperties']['RMSGradientDistribution'][0];
                                            $i = $step;
                                            while($i < sizeof($molecule['CalculatedProperties']['RMSGradientDistribution'])){
                                                echo "," . $molecule['CalculatedProperties']['RMSGradientDistribution'][round($i)];
                                                $i = $i + $step;
                                            }
                                            echo "]";
                                        }else{
                                            echo json_encode($molecule['CalculatedProperties']['RMSGradientDistribution']);
                                        }
                                    ?>
                        }
                    ]
                };
                var ctx1 = document.getElementById("gradientDistribution").getContext("2d");
                var options1 = {
                    legendTemplate : '<ul>'
                    +'<% for (var i=0; i<datasets.length; i++) { %>'
                    +'<li>'
                    +'<span style=\"background-color:<%=datasets[i].lineColor%>\"></span>'
                    +'<% if (datasets[i].label) { %><%= datasets[i].label %><% } %>'
                    +'</li>'
                    +'<% } %>'
                    +'</ul>'
                }
                var gradChart = new Chart(ctx1, options1).Line(gradientData, {multiTooltipTemplate: "<%= datasetLabel %> - <%= value %>"});
                var gradLegend = gradChart.generateLegend();
                $('#gradientDistribution').append(gradLegend);
                <?php endif; ?>

                var energyData = {
                    labels: <?php
                                if(sizeof($molecule['CalculatedProperties']['Iterations']) > 20){
                                    $step = sizeof($molecule['CalculatedProperties']['Iterations'])/20;
                                    echo "[" . $molecule['CalculatedProperties']['Iterations'][0];
                                    $i = $step;
                                    while(round($i) < sizeof($molecule['CalculatedProperties']['Iterations'])){
                                        echo "," . $molecule['CalculatedProperties']['Iterations'][round($i)];
                                        $i = $i + $step;
                                    }
                                    echo "]";
                                }else{
                                    echo json_encode($molecule['CalculatedProperties']['Iterations']);
                                }
                            ?>,
                    datasets: [
                        {
                            label: "Energy Distribution",
                            fillColor: "rgba(220,220,220,0.2)",
                            strokeColor: "rgba(220,220,220,1)",
                            pointColor: "rgba(220,220,220,1)",
                            pointStrokeColor: "#fff",
                            pointHighlightFill: "#fff",
                            pointHighlightStroke: "rgba(220,220,220,1)",
                            data: <?php
                                        if(sizeof($molecule['CalculatedProperties']['EnergyDistribution']) > 20){
                                            $step = sizeof($molecule['CalculatedProperties']['EnergyDistribution'])/20;
                                            echo "[" . $molecule['CalculatedProperties']['EnergyDistribution'][0];
                                            $i = $step;
                                            while(round($i) < sizeof($molecule['CalculatedProperties']['EnergyDistribution'])){
                                                echo "," . $molecule['CalculatedProperties']['EnergyDistribution'][round($i)];
                                                $i = $i + $step;
                                            }
                                            echo "]";
                                        }else{
                                            echo json_encode($molecule['CalculatedProperties']['EnergyDistribution']);
                                        }
                                    ?>
                        }
                    ]
                };
                <?php if(isset($molecule['CalculatedProperties']['EnergyDistribution'])):?>
                var ctx2 = document.getElementById("energyDistribution").getContext("2d");
                var options2 = {
                    legendTemplate : '<ul>'
                    +'<% for (var i=0; i<datasets.length; i++) { %>'
                    +'<li>'
                    +'<span style=\"background-color:<%=datasets[i].lineColor%>\"></span>'
                    +'<% if (datasets[i].label) { %><%= datasets[i].label %><% } %>'
                    +'</li>'
                    +'<% } %>'
                    +'</ul>'
                }
                var energyChart = new Chart(ctx2, options2).Line(energyData, {multiTooltipTemplate: "<%= datasetLabel %> - <%= value %>"});
                var energyLegend = energyChart.generateLegend();
                $('#energyDistribution').append(energyLegend);
                <?php endif; ?>
            });
        </script>
    </body>
</html>
<?php endif; ?>

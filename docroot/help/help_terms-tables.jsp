<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "DTD/xhtml1-transitional.dtd">
<html>
<head>
	<title>DataShop > Help > Terms > Transaction and Step tables</title>
	<meta content="text/html; charset=UTF-8" http-equiv="Content-Type" />
    <style type="text/css">
        body {
            font-family:"Arial";
        }
        table {
            font-size:.75em;
            margin-left:0;
            margin-bottom:2em;
            border-collapse:collapse;
        }
        table caption {
            font-weight:bold;
            margin-bottom:1em;
            font-family:Georgia,Times,serif;
            color:#333;
        }
        table thead th {
            font-size:1em;
            text-align:center;
        }
        
        table tbody td {
            border-bottom:1px solid gray;
            padding:.3em
        }
    </style>
<%@ include file="/google-analytics.jspf" %>
</head>

<body>

<div id="helpcontents">

<table id="table-1"><caption>Table-1: Transaction table excerpt</caption>
        <thead>
            <tr>
                <th>#</th>
                <th>Student</th>
                <th>Problem</th>
                <th>Step</th>
                <th>Attempt #</th>
                <th>Student Input</th>
                <th>Evaluation</th>
                <th>Knowledge component</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>1</td>
                <td>S01</td>
                <td>WATERING_VEGGIES</td>
                <td>(SPRAY-RADIUS Q1)</td>
                <td>1</td>
                <td>2</td>
                <td>CORRECT</td>
                <td>Enter-Given</td>
            </tr>
            <tr>
                <td>2</td>
                <td>S01</td>
                <td>WATERING_VEGGIES</td>
                <td>(WATERED-AREA Q1)</td>
                <td>1</td>
                <td>12.56</td>
                <td>CORRECT</td>
                <td>Circle-Area</td>
            </tr>
            <tr>
                <td>3</td>
                <td>S01</td>
                <td>WATERING_VEGGIES</td>
                <td>(TOTAL-GARDEN Q1)</td>
                <td>1</td>
                <td>39.43</td>
                <td>INCORRECT</td>
                <td></td>
            </tr>
            <tr>
                <td>4</td>
                <td>S01</td>
                <td>WATERING_VEGGIES</td>
                <td>(TOTAL-GARDEN Q1)</td>
                <td>2</td>
                <td>94.985</td>
                <td>INCORRECT</td>
                <td></td>
            </tr>
            <tr>
                <td>5</td>
                <td>S01</td>
                <td>WATERING_VEGGIES</td>
                <td>(TOTAL-GARDEN Q1)</td>
                <td>3</td>
                <td>HINT</td>
                <td>HINT</td>
                <td></td>
            </tr>
            <tr>
                <td>6</td>
                <td >S01</td>
                <td>WATERING_VEGGIES</td>
                <td>(TOTAL-GARDEN Q1)</td>
                <td>4</td>
                <td>30.25</td>
                <td>CORRECT</td>
                <td>Rectangle-Area</td>
            </tr>
            <tr>
                <td>7</td>
                <td>S01</td>
                <td>WATERING_VEGGIES</td>
                <td>(UNWATERED-AREA Q1)</td>
                <td>1</td>
                <td>17.69</td>
                <td>CORRECT</td>
                <td>Compose-Areas</td>
            </tr>
            <tr>
                <td>8</td>
                <td>S01</td>
                <td>WATERING_VEGGIES</td>
                <td>DONE</td>
                <td>1</td>
                <td>DONE</td>
                <td>CORRECT</td>
                <td>Determine-Done</td>
            </tr>
            <tr>
                <td>9</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(POG-RADIUS Q1)</td>
                <td>1</td>
                <td>4</td>
                <td>CORRECT</td>
                <td>Enter-Given</td>
            </tr>
            <tr>
                <td>10</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SQUARE-BASE Q1)</td>
                <td>1</td>
                <td>8</td>
                <td>CORRECT</td>
                <td>Enter-Given</td>
            </tr>
            <tr>
                <td>11</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SCRAP-METAL-AREA Q1)</td>
                <td>1</td>
                <td>32</td>
                <td>INCORRECT</td>
                <td></td>
            </tr>
            <tr>
                <td>12</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SCRAP-METAL-AREA Q1)</td>
                <td>2</td>
                <td>4</td>
                <td>INCORRECT</td>
                <td></td>
            </tr>
            <tr>
                <td>13</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SQUARE-AREA Q1)</td>
                <td>1</td>
                <td>64</td>
                <td>CORRECT</td>
                <td>Square-Area</td>
            </tr>
            <tr>
                <td>14</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(POG-AREA Q1)</td>
                <td>1</td>
                <td>50.24</td>
                <td>CORRECT</td>
                <td>Circle-Area</td>
            </tr>
            <tr>
                <td>15</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SCRAP-METAL-AREA Q1)</td>
                <td>3</td>
                <td>13.76</td>
                <td>CORRECT</td>
                <td>Compose-Areas</td>
            </tr>
            <tr>
                <td>16</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(POG-RADIUS Q2)</td>
                <td>1</td>
                <td>8</td>
                <td>CORRECT</td>
                <td>Enter-Given</td>
            </tr>
            <tr>
                <td>17</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SQUARE-BASE Q2)</td>
                <td>1</td>
                <td>16</td>
                <td>CORRECT</td>
                <td>Enter-Given</td>
            </tr>
            <tr>
                <td>18</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SQUARE-AREA Q2)</td>
                <td>1</td>
                <td>256</td>
                <td>CORRECT</td>
                <td>Square-Area</td>
            </tr>
            <tr>
                <td>19</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(POG-AREA Q2)</td>
                <td>1</td>
                <td>200.96</td>
                <td>CORRECT</td>
                <td>Circle-Area</td>
            </tr>
            <tr>
                <td>20</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SCRAP-METAL-AREA Q2)</td>
                <td>1</td>
                <td>55.04</td>
                <td>CORRECT</td>
                <td>Compose-Areas</td>
            </tr>
            <tr>
                <td>21</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(POG-RADIUS Q3)</td>
                <td>1</td>
                <td>12</td>
                <td>CORRECT</td>
                <td>Enter-Given</td>
            </tr>
            <tr>
                <td>22</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SQUARE-BASE Q3)</td>
                <td>1</td>
                <td>24</td>
                <td>CORRECT</td>
                <td>Enter-Given</td>
            </tr>
            <tr>
                <td>23</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SQUARE-AREA Q3)</td>
                <td>1</td>
                <td>576</td>
                <td>CORRECT</td>
                <td>Square-Area</td>
            </tr>
            <tr>
                <td>24</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(POG-AREA Q3)</td>
                <td>1</td>
                <td>452.16</td>
                <td>CORRECT</td>
                <td>Circle-Area</td>
            </tr>
            <tr>
                <td>25</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SCRAP-METAL-AREA Q3)</td>
                <td>1</td>
                <td>123.84</td>
                <td>CORRECT</td>
                <td>Compose-Areas</td>
            </tr>
            <tr>
                <td>26</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>DONE</td>
                <td>1</td>
                <td>DONE</td>
                <td>CORRECT</td>
                <td>Determine-Done</td>
            </tr>
        </tbody>
    </table>


<table id="table-2"><caption>Table-2: Step table excerpt</caption>
    <colgroup>
        <col />
        <col />
        <col />
        <col />
        <col style="width:12em" />
        <col style="width:10em" />
        <col />
        <col />
    </colgroup>
        <thead>
            <tr>
                <th>#</th>
                <th>Student</th>
                <th>Problem</th>
                <th>Step</th>
                <th>Opportunity Count</th>
                <th>Total Incorrects</th>
                <th>Total Hints</th>
                <th>Assistance Score</th>
                <th>Error Rate</th>
                <th>Knowledge component</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>1</td>
                <td>S01</td>
                <td>WATERING_VEGGIES</td>
                <td>(SPRAY-RADIUS Q1)</td>
                <td>1</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>Enter-Given</td>
            </tr>
            <tr>
                <td>2</td>
                <td>S01</td>
                <td>WATERING_VEGGIES</td>
                <td>(WATERED-AREA Q1)</td>
                <td>1</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>Circle-Area</td>
            </tr>
            <tr>
                <td>3</td>
                <td>S01</td>
                <td>WATERING_VEGGIES</td>
                <td>(TOTAL-GARDEN Q1)</td>
                <td>1</td>
                <td>2</td>
                <td>1</td>
                <td>3</td>
                <td>1</td>
                <td>Rectangle-Area</td>
            </tr>
            <tr>
                <td>4</td>
                <td>S01</td>
                <td>WATERING_VEGGIES</td>
                <td>(UNWATERED-AREA Q1)</td>
                <td>1</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>Compose-Areas</td>
            </tr>
            <tr>
                <td>5</td>
                <td>S01</td>
                <td>WATERING_VEGGIES</td>
                <td>DONE</td>
                <td>1</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>Determine-Done</td>
            </tr>
            <tr>
                <td>6</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(POG-RADIUS Q1)</td>
                <td>2</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>Enter-Given</td>
            </tr>
            <tr>
                <td>7</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SQUARE-BASE Q1)</td>
                <td>3</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>Enter-Given</td>
            </tr>            
            <tr>
                <td>8</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SQUARE-AREA Q1)</td>
                <td>1</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>Square-Area</td>
            </tr>
            <tr>
                <td>9</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(POG-AREA Q1)</td>
                <td>2</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>Circle-Area</td>
            </tr>
            <tr>
                <td>10</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SCRAP-METAL-AREA Q1)</td>
                <td>2</td>
                <td>2</td>
                <td>0</td>
                <td>2</td>
                <td>1</td>
                <td>Compose-Areas</td>
            </tr>
            <tr>
                <td>11</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(POG-RADIUS Q2)</td>
                <td>4</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>Enter-Given</td>
            </tr>
            <tr>
                <td>12</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SQUARE-BASE Q2)</td>
                <td>5</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>Enter-Given</td>
            </tr>              
            <tr>
                <td>13</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SQUARE-AREA Q2)</td>
                <td>2</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>Square-Area</td>
            </tr>
            <tr>
                <td>14</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(POG-AREA Q2)</td>
                <td>3</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>Circle-Area</td>
            </tr>
            <tr>
                <td>15</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SCRAP-METAL-AREA Q2)</td>
                <td>3</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>Compose-Areas</td>
            </tr>
            <tr>
                <td>16</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(POG-RADIUS Q3)</td>
                <td>6</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>Enter-Given</td>
            </tr>
            <tr>
                <td>17</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SQUARE-BASE Q3)</td>
                <td>7</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>Enter-Given</td>
            </tr>              
            <tr>
                <td>18</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SQUARE-AREA Q3)</td>
                <td>3</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>Square-Area</td>
            </tr>
            <tr>
                <td>19</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(POG-AREA Q3)</td>
                <td>4</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>Circle-Area</td>
            </tr>
            <tr>
                <td>20</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>(SCRAP-METAL-AREA Q3)</td>
                <td>4</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>Compose-Areas</td>
            </tr>
            <tr>
                <td>21</td>
                <td>S01</td>
                <td>MAKING-CANS</td>
                <td>DONE</td>
                <td>2</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>0</td>
                <td>Determine-Done</td>
            </tr>
            </tbody>
    </table>
    </div>
</body></html>

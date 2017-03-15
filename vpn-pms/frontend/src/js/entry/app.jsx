import ReactDOM from 'react-dom';
import React from 'react';

// import 'purecss/build/base-min.css'
// import 'purecss/build/pure-min.css'
// import 'purecss/build/grids-core-min.css.css'
import 'purecss/build/tables-min.css'

var div = document.getElementById('react-content');
var module = div.getAttribute("module");

if (module == 'main') {

    //   console.log("top-"+Topo);
    ReactDOM.render(
        <table className="pure-table">
            <thead>
            <tr>
                <th>#</th>
                <th>Make</th>
                <th>Model</th>
                <th>Year</th>
            </tr>
            </thead>

            <tbody>
            <tr>
                <td>1</td>
                <td>Honda</td>
                <td>Accord</td>
                <td>2009</td>
            </tr>

            <tr>
                <td>2</td>
                <td>Toyota</td>
                <td>Camry</td>
                <td>2012</td>
            </tr>

            <tr>
                <td>3</td>
                <td>Hyundai</td>
                <td>Elantra</td>
                <td>2010</td>
            </tr>
            </tbody>
        </table>

        ,div);
}

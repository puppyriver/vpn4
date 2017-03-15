/* global __dirname */

var path = require('path');

var webpack = require('webpack');
var CopyWebpackPlugin = require('copy-webpack-plugin');

var dir_js = path.resolve(__dirname, 'src/js/entry');
var dir_html = path.resolve(__dirname, '../src/main/resources/static');
var dir_build = path.resolve(__dirname, '../src/main/resources/static');

module.exports = {

    //entry: path.resolve(dir_js, 'test.jsx'),
    //entry : {
    //    "dev1" : "webpack-dev-server/client?http://0.0.0.0:3000",
    //    "dev2" : "webpack/hot/only-dev-server",
    //    "app" :  [path.resolve(dir_js, 'app.jsx')]
    //   //"customer" : [path.resolve(dir_js, 'customer.jsx')],
    //   // "users" :[path.resolve(dir_js, 'users.jsx')]
    //},


    entry: [
        // 写在入口文件之前
        "webpack-dev-server/client?http://0.0.0.0:6060",
        "webpack/hot/only-dev-server",
        // 这里是你的入口文件
        path.resolve(dir_js, 'app.jsx')
    ],

    //entry: [
    //    'webpack-dev-server/client?http://localhost:9090',
    //    'webpack/hot/only-dev-server'
    //    ,path.resolve(dir_js, 'test.jsx')],
    output: {
        path: dir_build,
        //filename: '[name].js'
        filename: 'app.js'
    },
    devServer: {
    	outputPath :  path.resolve(__dirname, '../server/client'),
        contentBase: dir_build,
        hot: true,
        inline: true,
        proxy: {
            '/ajax1/*': 'http://127.0.0.1:8080/itmanager',
            '/ajax/*': 'http://127.0.0.1:9000',
            '/cdcp/*': 'http://127.0.0.1:8000'
        }
    },
    module: {
        loaders: [
            {
                loaders: ['react-hot'],
                //test: dir_js,
                test : /\.jsx?$/,
                exclude: /node_modules/
            },
            {
                loader: 'babel-loader',
                test: /\.jsx?$/,
                query: {
                    presets: ['es2015', 'react', 'stage-0'],
                },
            },
            {
                loader : 'style-loader!css-loader',
                test : /\.css$/,
            },{ test: /\.png$/, loader: 'file-loader?name=img/[name].[ext]' }
        ]
    },
    plugins: [
        new webpack.optimize.UglifyJsPlugin({
            compress: {
                //supresses warnings, usually from module minification
                warnings: false
            }
        }),
        // Simply copies the files over
        new CopyWebpackPlugin([
            { from: dir_html } // to: output.path
        ]),
        // Avoid publishing files when compilation fails
        new webpack.NoErrorsPlugin(),
        new webpack.HotModuleReplacementPlugin(),
        new webpack.optimize.OccurenceOrderPlugin(),
        new webpack.DefinePlugin({
            "process.env": {
                NODE_ENV: JSON.stringify("production")
            }
        })

    ],
    stats: {
        // Nice colored output
        colors: true
    },
    cache: true,
    // Create Sourcemaps for the bundle
    devtool : ['eval']
    //devtool : ['cheap-module-source-map']
    // devtool: 'cheap-module-eval-source-map'
};



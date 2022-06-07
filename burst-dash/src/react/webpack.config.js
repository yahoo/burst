const path = require('path');
const {DefinePlugin} = require('webpack');
const {BundleAnalyzerPlugin} = require('webpack-bundle-analyzer');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

const prodBuild = '../main/resources/static';
const hotBuild = '../../target/classes/static';
module.exports = ({localDev = false, profile} = {}) => {
    const plugins = [
        new MiniCssExtractPlugin({filename: "[name].css", chunkFilename: '[name].css'}),
        new DefinePlugin({
            'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV || 'development')
        })
    ];
    if (profile) plugins.push(new BundleAnalyzerPlugin());
    return {
        mode: localDev ? 'development' : 'production',
        entry: ['./app/burst-main.jsx'],
        cache: {type: 'filesystem'},
        devtool: "source-map",
        output: {
            publicPath: "/static/",
            path: `${__dirname}/${localDev ? hotBuild : prodBuild}`,
            filename: '[name].js'
        },
        optimization: {
            minimize: !localDev,
            splitChunks: {
                cacheGroups: {
                    vendor: {
                        test: /[\\/]node_modules[\\/]/,
                        name: 'vendors',
                        chunks: 'all'
                    }
                }
            }
        },
        plugins,
        resolve: {
            modules: [
                path.resolve('./app'),
                path.resolve('./node_modules')
            ],
            extensions: ['.js', '.jsx', '.json', '.css']
        },
        module: {
            rules: [
                {
                    test: /\.jsx?$/,
                    exclude: /(node_modules)/,
                    loader: 'babel-loader'
                },
                {
                    test: /\.(sc|sa|c)?ss$/,
                    use: [MiniCssExtractPlugin.loader, 'css-loader', 'sass-loader'],
                    sideEffects: true
                },
                {
                    // Match woff2 and patterns like .woff?v=1.1.1.
                    test: /eot|ttf|\.woff2?(\?v=\d+\.\d+\.\d+)?$/,
                    type: 'asset/resource'
                },
                {
                    test: /.(png|jpg|jpeg|gif|svg)$/,
                    loader: "url-loader",
                    options: {
                        limit: 100000
                    },
                    sideEffects: true
                }
            ]
        }
    };
};

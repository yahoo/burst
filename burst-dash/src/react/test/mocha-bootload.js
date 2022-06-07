require('@babel/register')({
  extensions: ['.js', '.jsx'],
  plugins: [
    'babel-plugin-rewire',
  ],
  cache: false,
});

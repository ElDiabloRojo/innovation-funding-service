var gulp = require('gulp')

var subProjects = [
  'ifs-application-service',
  'ifs-assessment-service',
  'ifs-competition-mgt-service',
  'ifs-project-setup-service',
  'ifs-project-setup-mgt-service',
  'eu-grant-registration-service',
  'ifs-web-core']

subProjects.forEach(function(subProject) {
  require('./' + subProject + '/src/main/resources/static/gulpfile.js')
})

gulp.task('js', gulp.parallel('application:js','assessment:js','competition-mgt:js','eu-grant-registration:js','project-setup:js','project-setup-management:js','web-core:js'))

gulp.task('css', gulp.parallel('application:css','assessment:css','competition-mgt:css','project-setup:css','project-setup-management:css','web-core:css'))

gulp.task('css:watch', gulp.parallel('application:css:watch','assessment:css:watch','competition-mgt:css:watch','project-setup:css:watch','project-setup-management:css:watch','web-core:css:watch'))

gulp.task('default', gulp.parallel('js','css'))

package com.yotpo.metorikku.metric

import java.io.File

import com.yotpo.metorikku.configuration.metric.Step
import com.yotpo.metorikku.exceptions.MetorikkuException
import com.yotpo.metorikku.metric.stepActions.{Code, Sql}
import com.yotpo.metorikku.utils.FileUtils

object StepFactory {

  def getStepAction(configuration: Step, metricDir: Option[File], metricName: String,
                    showPreviewLines: Int, cacheOnPreview: Option[Boolean],
                    showQuery: Option[Boolean], dqConfigurator: DeequFactory): StepAction[_] = {
    configuration.sql match {
      /** 处理简单的 sql 语句 */
      case Some(expression) => Sql(expression,
        configuration.dataFrameName,
        showPreviewLines, cacheOnPreview,
        showQuery,
        dqConfigurator.generateDeequeList(configuration.dq))

      case None => {
        configuration.file match {
          /** 处理 sql 文件 */
          case Some(filePath) =>
            val path = metricDir match {
              case Some(dir) => new File(dir, filePath).getPath
              case _ => filePath
            }
            Sql(
              FileUtils.readConfigurationFile(path),
              configuration.dataFrameName,
              showPreviewLines,
              cacheOnPreview,
              showQuery,
              dqConfigurator.generateDeequeList(configuration.dq))

          case None => {
            configuration.classpath match {
                /** 处理 udf 的 */
              case Some(cp) => {
                Code(cp, metricName, configuration.dataFrameName, configuration.params)
              }
              case None => throw MetorikkuException("Each step requires an SQL query or a path to a file (SQL/Scala)")
            }
          }
        }
      }
    }
  }
}

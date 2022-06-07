/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.relate.pool

import org.apache.commons.dbcp2.{DriverManagerConnectionFactory, PoolableConnection, PoolableConnectionFactory, PoolingDataSource}
import org.apache.commons.pool2.impl.{AbandonedConfig, GenericObjectPool}
import scalikejdbc.{ConnectionPool, ConnectionPoolSettings}

import java.sql.Connection
import javax.sql.DataSource

/**
  * Commons DBCP Connection Pool
  *
  * @see [[http://commons.apache.org/dbcp/]]
  */
class RelateCommons2ConnectionPool(
                                    override val url: String,
                                    override val user: String,
                                    password: String,
                                    override val settings: ConnectionPoolSettings = ConnectionPoolSettings()
                                  )
  extends ConnectionPool(url, user, password, settings) {

  private[this] val _poolFactory = new PoolableConnectionFactory(
    new DriverManagerConnectionFactory(url, user, password), null
  )
  _poolFactory.setValidationQuery(settings.validationQuery)

  private[this] val _pool: GenericObjectPool[PoolableConnection] = new GenericObjectPool(_poolFactory)
  _poolFactory.setPool(_pool)

  _pool.setMinIdle(settings.initialSize)
  _pool.setMaxIdle(settings.maxSize)
  _pool.setBlockWhenExhausted(true)
  _pool.setMaxTotal(settings.maxSize)
  _pool.setMaxWaitMillis(settings.connectionTimeoutMillis)
  _pool.setMinEvictableIdleTimeMillis(10000)
  _pool.setTimeBetweenEvictionRunsMillis(10000)
  val abandonConfig = new AbandonedConfig()
  abandonConfig.setLogAbandoned(true)
  abandonConfig.setRemoveAbandonedOnMaintenance(true)
  abandonConfig.setUseUsageTracking(true)
  _pool.setAbandonedConfig(abandonConfig)

  // To fix MS SQLServer jtds driver issue
  // https://github.com/scalikejdbc/scalikejdbc/issues/461
  if (Option(settings.validationQuery).exists(_.trim.nonEmpty)) {
    _pool.setTestOnBorrow(true)
  }

  private[this] val _dataSource: DataSource = new PoolingDataSource(_pool)

  override def dataSource: DataSource = _dataSource

  override def borrow(): Connection = dataSource.getConnection()

  override def numActive: Int = _pool.getNumActive

  override def numIdle: Int = _pool.getNumIdle

  override def maxActive: Int = _pool.getMaxTotal

  override def maxIdle: Int = _pool.getMaxIdle

  override def close(): Unit = _pool.close()

}


/*
 * Copyright (c) 2018 Mirai Solutions GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miraisolutions.spark.bigquery

import com.google.cloud.bigquery.{Table, TableId}
import com.miraisolutions.spark.bigquery.exception.ParseException
import scala.language.implicitConversions

/**
  * BigQuery table reference
  * @param project Project ID
  * @param dataset Dataset ID
  * @param table Table ID
  */
private final case class BigQueryTableReference(project: String, dataset: String, table: String) {

  /** Returns the unquoted table identifier (BigQuery Standard SQL) */
  def unquotedIdentifier: String = s"$project.$dataset.$table"
  /** Returns the quoted table identifier (BigQuery Standard SQL) */
  def quotedIdentifier: String = "`" + unquotedIdentifier + "`"

  /** BigQuery Standard SQL table identifier (quoted) */
  override def toString: String = quotedIdentifier
}

private object BigQueryTableReference {

  /**
    * Creates a [[BigQueryTableReference]] from a [[TableId]]
    * @param tableId Table ID
    * @return BigQuery table reference
    */
  def apply(tableId: TableId): BigQueryTableReference =
    BigQueryTableReference(tableId.getProject, tableId.getDataset, tableId.getTable)

  /**
    * Creates a [[BigQueryTableReference]] from a table reference [[String]]
    * @param tableRef Table reference string
    * @return BigQuery table reference
    */
  def apply(tableRef: String): BigQueryTableReference = {
    val tableId = raw"((.+:)?[\w_\-]+)\.([\w_\-]+)\.([\w_\-]+)".r
    tableRef.replace("`", "") match {
      case tableId(project, _, dataset, table) => BigQueryTableReference(project, dataset, table)
      case _ => throw new ParseException("Failed to parse BigQuery table reference which needs to be of the form " +
        "[projectId].[datasetId].[tableId]")
    }
  }

  // Converts an internal BigQuery table reference to a Google BigQuery API `TableId`
  implicit def bigQueryTableReferenceToTableId(table: BigQueryTableReference): TableId = {
    TableId.of(table.project, table.dataset, table.table)
  }

  // Converts a Google BigQuery API `TableId` to an internal BigQuery table reference
  implicit def tableIdToBigQueryTableReference(tableId: TableId): BigQueryTableReference = apply(tableId)

  // Converts a Google BigQuery API `Table` to an internal BigQuery table reference
  implicit def tableToBigQueryTableReference(table: Table): BigQueryTableReference = apply(table.getTableId)
}

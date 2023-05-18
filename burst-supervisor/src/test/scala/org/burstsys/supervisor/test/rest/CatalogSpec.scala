/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.test.rest


class CatalogSpec extends SupervisorRestSpecBase {

  ///////////////////////////////////////////////
  // Queries
  ///////////////////////////////////////////////

  it should "fetch all queries" in {
    val nodes = fetchArrayFrom("/catalog/allQueries", method = "POST")
    nodes.length shouldBe 9
  }

  ignore should "be able to create, update, and delete queries"

  ///////////////////////////////////////////////
  // Domains
  ///////////////////////////////////////////////

  it should "fetch all domains" in {
    val domains = fetchArrayFrom("/catalog/domains")
    domains.length shouldBe 7
  }

  it should "fetch a set of domains" in {
    val domains = fetchArrayFrom("/catalog/domains?pks=1,2")
    domains.length shouldBe 2
  }

  it should "fetch a single domain" in {
    val domain = fetchObjectFrom("/catalog/domains/1")
    assertJsonContains(
      domain,
      Map("pk" -> 1, "moniker" -> "Domain1")
    )
  }

  it should "fetch all views for a domain" in {
    val views = fetchArrayFrom("/catalog/domains/1/views")
    views.length shouldBe 2

    val view1 = views.head
    view1.get("pk").asLong shouldBe 1
    view1.get("moniker").asText shouldBe "Domain1View1"
  }

  it should "be able to create a domain with form data" in {
    val domain = sendFormDataTo("/catalog/newDomain", Map("moniker" -> "New Form Domain"))
    val pk = domain.get("pk").asLong
    pk should be > 0L

    val fetched = fetchObjectFrom(s"/catalog/domains/$pk")
    assertJsonContains(
      fetched,
      Map("moniker" -> "New Form Domain", "udk" -> null)
    )
  }

  it should "be able to create a domain with UDK" in {
    val domain = sendJsonDataTo("/catalog/domain", Map("moniker" -> "New Json Domain", "udk" -> "My_UDK"))
    val pk = domain.get("pk").asLong
    pk should be > 0L

    assertJsonContains(
      fetchObjectFrom(s"/catalog/domains/$pk"),
      Map("moniker" -> "New Json Domain", "udk" -> "My_UDK")
    )
  }

  it should "be able to upsert a domain by UDK" in {
    val udk = "My_UDK2"
    val domain = sendJsonDataTo("/catalog/domain", Map("moniker" -> "New Json Domain 2", "udk" -> udk))
    val pk = domain.get("pk").asLong
    pk should be > 0L

    assertJsonContains(
      fetchObjectFrom(s"/catalog/domains/$pk"),
      Map("pk" -> pk, "moniker" -> "New Json Domain 2", "udk" -> udk)
    )

    val upsert = sendJsonDataTo("/catalog/domain", Map("moniker" -> "Upsert Json Domain 2", "udk" -> udk))
    assertJsonContains(
      fetchObjectFrom(s"/catalog/domains/$pk"),
      Map("pk" -> pk, "moniker" -> "Upsert Json Domain 2", "udk" -> udk)
    )
  }

  it should "be able to upsert a domain by PK" in {
    val udk = "My_UDK3"
    val created = sendJsonDataTo("/catalog/domain", Map("moniker" -> "New Json Domain 3", "udk" -> udk))
    val pk = created.get("pk").asLong
    pk should be > 0L

    assertJsonContains(
      fetchObjectFrom(s"/catalog/domains/$pk"),
      Map("pk" -> pk, "moniker" -> "New Json Domain 3", "udk" -> udk)
    )

    val upsert = sendJsonDataTo("/catalog/domain", Map("pk" -> pk, "moniker" -> "Upsert Json Domain 3", "udk" -> udk))
    assertJsonContains(
      fetchObjectFrom(s"/catalog/domains/$pk"),
      Map("pk" -> pk, "udk" -> udk, "moniker" -> "Upsert Json Domain 3")
    )
  }

  ignore should "be able to delete a view"

  ///////////////////////////////////////////////
  // Views
  ///////////////////////////////////////////////

  it should "fetch a single view" in {
    assertJsonContains(
      fetchObjectFrom("/catalog/viewByPk", "POST", Map("pk" -> "1")),
      Map("pk" -> 1, "moniker" -> "Domain1View1", "schemaName" -> "quo", "domainFk" -> 1, "udk" -> "vudk01")
    )
  }

  it should "be able to create a view with form data" in {
    val created = sendFormDataTo("/catalog/newView", Map("moniker" -> "Moniker", "domainPk" -> 1, "schemaName" -> "quo"))
    val pk = created.get("pk").asLong
    pk should be > 0L

    val view = fetchObjectFrom("/catalog/viewByPk", "POST", Map("pk" -> s"$pk"))
    assertJsonContains(
      view,
      Map("pk" -> pk, "moniker" -> "Moniker", "schemaName" -> "quo", "domainFk" -> 1)
    )
  }
}

# NewPackageIvy
Bachelor Thesis

The XML file:
<log>
  <event-types>
    <event-type name="create-order">
      <attributes>
        <attribute name="total-items" type="integer"/>
      </attributes>
    </event-type>
  </event-types>
  <object-types>
    <object-type name="order">
      <attributes>
        <attribute name="item" type="integer"/>
      </attributes>
    </object-type>
  </object-types>
  <events>
    <event id="e1" type="create-order" time="2023-10-16T15:30:00Z">
      <attributes>
        <attribute name="total-items">1</attribute>
      </attributes>
      <objects>
        <relationship object-id="o1" relationship="order"/>
      </objects>
    </event>
  </events>
  <objects>
    <object id="o1" type="order">
      <attributes>
        <attribute name="item" time="1970-01-01T00:00:00Z">1</attribute>
      </attributes>
    </object>
  </objects>
</log>

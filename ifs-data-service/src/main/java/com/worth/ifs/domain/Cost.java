package com.worth.ifs.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Cost {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    String item;
    String description;
    Integer quantity;
    Double cost;

    @OneToMany(mappedBy="cost")
    private List<CostValue> costValues = new ArrayList<CostValue>();

    @ManyToOne
    @JoinColumn(name="costCategoryId", referencedColumnName="id")
    private CostCategory costCategory;

    public Cost() {
    }

    public Cost(String item, String description, Integer quantity, Double cost, CostCategory costCategory) {
        this.item = item;
        this.description = description;
        this.quantity = quantity;
        this.cost = cost;
        this.costCategory = costCategory;
    }

    public Cost(Long id, String item, String description, Integer quantity, Double cost, CostCategory costCategory) {
        this(item, description, quantity, cost, costCategory);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getItem() {
        return item;
    }

    public String getDescription() {
        return description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Double getCost() {
        return cost;
    }

    public void setCostCategory(CostCategory costCategory) {
        this.costCategory = costCategory;
    }
}


package com.productsstores.services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productsstores.entity.Product;

public interface ProductRepository extends JpaRepository<Product,Integer>
{
	

}

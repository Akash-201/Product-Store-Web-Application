package com.productsstores.controllers;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.productsstores.entity.Product;
import com.productsstores.entity.ProductDto;
import com.productsstores.services.ProductRepository;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;




@Controller
@RequestMapping("/products")
public class ProductsController 
{
	
	@Autowired
	private ProductRepository productRepository;
	
	
	@GetMapping({"","/"})
	public String showProductList(Model model)
	{
		List<Product> products=productRepository.findAll();
		model.addAttribute("products",products);
		return "index1";
	}
	
	@GetMapping("/create")
	public String showCreatePage(Model model)
	{
		ProductDto productDto=new ProductDto();
		model.addAttribute("productDto", productDto);
		return "createProduct";
	}
	
	@PostMapping("/create")
	public String createProduct(@Valid @ModelAttribute ProductDto productDto, BindingResult bindingResult)
	{
	    if (productDto.getImageFile().isEmpty()) 
	    {
	        bindingResult.addError(new FieldError("productDto", "imageFile", "The image file is required"));
	    }

	    if (bindingResult.hasErrors()) 
	    {
	        return "createProduct";
	    }

	     // save images in database
	    
	    MultipartFile image = productDto.getImageFile();
	    Date createdAt = new Date();
	    String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

	    try
	    {
	        String uploadDir = "src/main/resources/static/images/";
	        Path uploadPath = Paths.get(uploadDir);

	        if (!Files.exists(uploadPath)) 
	        {
	            Files.createDirectories(uploadPath);
	        }

	        try (InputStream inputStream = image.getInputStream()) 
	        {
	            Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
	            System.out.println("Image saved to: " + uploadDir + storageFileName); // Debugging log
	        }

	    }
	    catch (Exception e)
	    {
	        System.out.println("Exception: " + e.getMessage());
	    }

	    Product product = new Product();
	    product.setName(productDto.getName());
	    product.setBrand(productDto.getBrand());
	    product.setCategory(productDto.getCategory());
	    product.setPrice(productDto.getPrice());
	    product.setDescription(productDto.getDescription());
	    product.setCreatedAt(createdAt);
	    product.setImageFileName(storageFileName);

	    productRepository.save(product);

	    return "redirect:/products";
	}
	
	@GetMapping("/edit")
	public String showEditPage(Model model , @RequestParam int id)
	{
		try 
		{
			Product product =productRepository.findById(id).get();
			model.addAttribute("product",product);
			
			ProductDto productDto=new ProductDto();
			   productDto.setName(product.getName());
			    productDto.setBrand(product.getBrand());
			    productDto.setCategory(product.getCategory());
			    productDto.setPrice(product.getPrice());
			    productDto.setDescription(product.getDescription());
			    
			    model.addAttribute("productDto",productDto);
			
		} 
		catch (Exception e) 
		{
			System.out.println("Exception: "+ e.getMessage());
			return "redirect:/products";
		}
		return "editProduct";
	}
	
	
	@PostMapping("/edit")
	public String updateProduct(Model model,@RequestParam int id, @Valid @ModelAttribute ProductDto productDto, BindingResult result)
	{

		
		try
		{
			Product product=productRepository.findById(id).get();
			model.addAttribute("product",product);
			
			if(result.hasErrors())
			{
				return "editProduct";
			}
			
			if(!productDto.getImageFile().isEmpty())
			{
				//delete old image
				String uploadDir="/static/images/";
				Path oldImagePath=Paths.get(uploadDir + product.getImageFileName());
				 
				try
				{
					Files.delete(oldImagePath);
					
				} catch (Exception e)
				
				{
					System.out.println("Exception: "+ e.getMessage());
				}
				
				// save new image
				MultipartFile image=productDto.getImageFile();
				Date createdAt=new Date();
				String storageFileName=createdAt.getTime() + "_" +image.getOriginalFilename();
				
				try(InputStream inputStream =image.getInputStream())
				{
					Files.copy(inputStream,Paths.get(uploadDir,storageFileName),StandardCopyOption.REPLACE_EXISTING);
				}
				product.setImageFileName(storageFileName);
				
			}
			    product.setName(productDto.getName());
			    product.setBrand(productDto.getBrand());
			    product.setCategory(productDto.getCategory());
			    product.setPrice(productDto.getPrice());
			    product.setDescription(productDto.getDescription());
			    
			    productRepository.save(product);
			
		} 
		catch (Exception e)
		{
			System.out.println("Exception: "+e.getMessage());

		}
		
		return "redirect:/products";
	}
	
	// delete products
	
	@GetMapping("/delete")
	public String deleteProduct(@RequestParam int id) {
	    try {
	        Optional<Product> optionalProduct = productRepository.findById(id);
	        if (!optionalProduct.isPresent()) {
	            System.out.println("Product with ID " + id + " not found");
	            return "redirect:/products";
	        }

	        Product product = optionalProduct.get();

	        // Delete product image
	        Path imagePath = Paths.get("src/main/resources/static/images/" + product.getImageFileName());
	        try {
	            Files.delete(imagePath);
	        } catch (Exception e) {
	            System.out.println("Exception while deleting image: " + e.getMessage());
	        }

	        // Delete product from database
	        productRepository.delete(product);
	    } catch (Exception e) {
	        System.out.println("Exception while deleting product: " + e.getMessage());
	    }
	    return "redirect:/products";
	}

	
	

	
	
	
	

}

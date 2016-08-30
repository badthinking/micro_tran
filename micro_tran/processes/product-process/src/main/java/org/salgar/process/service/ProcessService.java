package org.salgar.process.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.salgar.annotation.TransactionalFanout;
import org.salgar.process.context.OrderContext;
import org.salgar.process.facade.ProcessFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestController
@Transactional
@TransactionalFanout( services = {"proxyProductService"})
public class ProcessService {
	private final static Log LOG = LogFactory.getLog(ProcessService.class);
	private boolean routeRestProduct = false;
	private boolean routeRestCustomer = false;
	private boolean routeRestOrder = false;

	@Autowired(required = false)
	@Named("proxyProductService")
	private org.salgar.product.api.ProductService productService;
	
	@Autowired(required = false)
	@Named("proxyCustomerService")
	private org.salgar.customer.api.CustomerService customerService;
	
	@Autowired(required = false)
	@Named("proxyOrderService")
	private org.salgar.order.api.OrderService orderService;

	@Autowired
	private ProcessFacade processFacade;
	
	@PostConstruct
	private void defineRoutes() {
		if(productService == null) {
			routeRestProduct = true;
		} else {
			try {
				String healthCheck = productService.giveAlive();
				if(healthCheck == null && "".equals(healthCheck)) {
					routeRestProduct = true;
				}
			} catch (Throwable t) {
				LOG.error(t.getMessage(), t);
				routeRestProduct = true;
			}
		}
		
		if(customerService == null) {
			routeRestCustomer = true;
		} else {
			try {
				String healthCheck = customerService.giveAlive();
				if(healthCheck == null && "".equals(healthCheck)) {
					routeRestCustomer = true;
				}
			} catch (Throwable t) {
				LOG.error(t.getMessage(), t);
				routeRestCustomer = true;
			}
		}
		
		if(orderService == null) {
			routeRestOrder = true;
		} else {
			try {
				String healthCheck = orderService.giveAlive();
				if(healthCheck == null && "".equals(healthCheck)) {
					routeRestOrder = true;
				}
			} catch (Throwable t) {
				LOG.error(t.getMessage(), t);
				routeRestOrder = true;
			}
		}
	}

	@RequestMapping("/product/{productId}")
	@Transactional(readOnly = true)
	public org.salgar.product.api.model.Product getProduct(@PathVariable int productId)
			throws JsonParseException, JsonMappingException, IOException {
		if (routeRestProduct) {
			return processFacade.executeFallBackProduct(productId);
		}

		org.salgar.product.api.model.Product resut = processFacade.giveProduct(productId);

		return resut;
	}
	
	@RequestMapping(path = "/product/saveProduct", method = RequestMethod.POST)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public org.salgar.product.api.model.Product saveProduct(@RequestBody org.salgar.product.api.model.Product product)
			throws JsonParseException, JsonMappingException, IOException {
		if (routeRestProduct) {
			return processFacade.executeFallBackSaveProduct(product);
		}

		org.salgar.product.api.model.Product resut = processFacade.saveProduct(product);

		return resut;
	}
	
	@RequestMapping("/customer/{customerId}")
	@Transactional(readOnly = true)
	public org.salgar.customer.api.model.Customer getCustomer(@PathVariable int customerId)
			throws JsonParseException, JsonMappingException, IOException {
		if (routeRestCustomer) {
			return processFacade.executeFallBackCustomer(customerId);
		}

		org.salgar.customer.api.model.Customer resut = processFacade.giveCustomer(customerId);

		return resut;
	}
	
	@RequestMapping(path = "/customer/saveCustomer", method = RequestMethod.POST)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public org.salgar.customer.api.model.Customer saveCustomer(@RequestBody org.salgar.customer.api.model.Customer customer)
			throws JsonParseException, JsonMappingException, IOException {
		if (routeRestCustomer) {
			return processFacade.executeFallBackSaveCustomer(customer);
		}

		org.salgar.customer.api.model.Customer resut = processFacade.saveCustomer(customer);

		return resut;
	}
	
	@RequestMapping("/order/{orderId}")
	@Transactional(readOnly = true)
	public org.salgar.order.api.model.Order getOrder(@PathVariable int orderId)
			throws JsonParseException, JsonMappingException, IOException {
		if (routeRestOrder) {
			return processFacade.executeFallBackOrder(orderId);
		}

		org.salgar.order.api.model.Order resut = processFacade.giveOrder(orderId);

		return resut;
	}
	
	@RequestMapping(path = "/order/saveOrder", method = RequestMethod.POST)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public org.salgar.order.api.model.Order saveOrder(@RequestBody org.salgar.order.api.model.Order order)
			throws JsonParseException, JsonMappingException, IOException {
		if (routeRestOrder) {
			return processFacade.executeFallBackSaveOrder(order);
		}

		org.salgar.order.api.model.Order resut = processFacade.saveOrder(order);

		return resut;
	}
	
	@RequestMapping(path = "/saveOrderWProductWCustomer", method = RequestMethod.POST)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public org.salgar.order.api.model.Order saveOrderWithProductWithCustomer(@RequestBody OrderContext orderContext)
			throws JsonParseException, JsonMappingException, IOException {
		try {
			org.salgar.customer.api.model.Customer customerInternal = null;

			if (routeRestCustomer) {
				customerInternal = processFacade.executeFallBackCustomer(orderContext.getCustomer().getId());
			} else {
				customerInternal = processFacade.giveCustomer(orderContext.getCustomer().getId());
			}
			org.salgar.product.api.model.Product productInternal;
			if (routeRestProduct) {
				productInternal = processFacade.executeFallBackProduct(orderContext.getProduct().getProductId());
			} else {
				productInternal = processFacade.giveProduct(orderContext.getProduct().getProductId());
			}

			List<org.salgar.product.api.model.Product> products = new ArrayList<org.salgar.product.api.model.Product>();
			products.add(productInternal);
			orderContext.getOrder().setProducts(products);
			orderContext.getOrder().setCustomer(customerInternal);

			if (routeRestOrder) {
				return processFacade.executeFallBackSaveOrder(orderContext.getOrder());
			} else {
				return processFacade.saveOrder(orderContext.getOrder());
			}
		} catch (Throwable t) {
			LOG.error(t.getMessage(), t);
			throw t;
		}
	}
	
	@RequestMapping(path = "/saveOrderWithProduct", method = RequestMethod.POST)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public org.salgar.order.api.model.Order saveOrderWithAnExistingProduct(@RequestBody org.salgar.order.api.model.Order order,
			@RequestParam("productId") Integer productId) throws JsonParseException, JsonMappingException, IOException {
		try {
			org.salgar.product.api.model.Product product;
			if (routeRestProduct) {
				product = processFacade.executeFallBackProduct(productId);
			} else {
				product = processFacade.giveProduct(productId);
			}

			order.getProducts().add(product);
			if (routeRestOrder) {
				return processFacade.executeFallBackSaveOrder(order);
			} else {
				return processFacade.saveOrder(order);
			}
		} catch (Throwable t) {
			LOG.error(t.getMessage(), t);
			throw t;
		}
	}
}
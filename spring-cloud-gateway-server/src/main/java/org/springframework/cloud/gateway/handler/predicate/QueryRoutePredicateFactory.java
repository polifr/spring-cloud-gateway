/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.gateway.handler.predicate;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author Spencer Gibb
 */
public class QueryRoutePredicateFactory extends AbstractRoutePredicateFactory<QueryRoutePredicateFactory.Config> {

	/**
	 * Param key.
	 */
	public static final String PARAM_KEY = "param";

	/**
	 * Regexp key.
	 */
	public static final String REGEXP_KEY = "regexp";

	/**
	 * Predicate key.
	 */
	public static final String PREDICATE_KEY = "predicate";

	public QueryRoutePredicateFactory() {
		super(Config.class);
	}

	@Override
	public List<String> shortcutFieldOrder() {
		return Arrays.asList(PARAM_KEY, REGEXP_KEY, PREDICATE_KEY);
	}

	@Override
	public Predicate<ServerWebExchange> apply(Config config) {
		return new GatewayPredicate() {
			@Override
			public boolean test(ServerWebExchange exchange) {
				if (!StringUtils.hasText(config.regexp) && config.predicate == null) {
					// check existence of header
					return exchange.getRequest().getQueryParams().containsKey(config.param);
				}

				List<String> values = exchange.getRequest().getQueryParams().get(config.param);
				if (values == null) {
					return false;
				}

				Predicate<String> predicate = config.predicate;
				if (StringUtils.hasText(config.regexp)) {
					predicate = value -> value.matches(config.regexp);
				}
				for (String value : values) {
					if (value != null && predicate.test(value)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public Object getConfig() {
				return config;
			}

			@Override
			public String toString() {
				return String.format("Query: param=%s regexp=%s", config.getParam(), config.getRegexp());
			}
		};
	}

	@Validated
	public static class Config {

		@NotEmpty
		private String param;

		private String regexp;

		private Predicate<String> predicate;

		public String getParam() {
			return param;
		}

		public Config setParam(String param) {
			this.param = param;
			return this;
		}

		public String getRegexp() {
			return regexp;
		}

		public Config setRegexp(String regexp) {
			this.regexp = regexp;
			return this;
		}

		public Predicate<String> getPredicate() {
			return predicate;
		}

		public Config setPredicate(Predicate<String> predicate) {
			this.predicate = predicate;
			return this;
		}

	}

}

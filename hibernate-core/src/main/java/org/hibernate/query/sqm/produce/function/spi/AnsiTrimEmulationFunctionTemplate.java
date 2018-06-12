/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.produce.function.spi;

import java.util.Arrays;
import java.util.List;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.model.domain.spi.AllowableFunctionReturnType;
import org.hibernate.query.sqm.produce.function.SqmFunctionTemplate;
import org.hibernate.query.sqm.produce.spi.TrimSpecificationExpressionWrapper;
import org.hibernate.query.sqm.tree.expression.SqmLiteralCharacter;
import org.hibernate.query.sqm.tree.expression.SqmLiteralString;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.function.SqmFunction;
import org.hibernate.query.sqm.tree.expression.function.SqmGenericFunction;
import org.hibernate.sql.ast.tree.spi.TrimSpecification;
import org.hibernate.type.spi.StandardSpiBasicTypes;

/**
 * A {@link SqmFunctionTemplate} implementation that emulates the ANSI SQL trim function
 * on dialects which do not support the full definition.  However, this function
 * definition does assume the availability of ltrim, rtrim, and replace functions
 * which it uses in various combinations to emulate the desired ANSI trim()
 * functionality.
 *
 * @author Steve Ebersole
 */
public class AnsiTrimEmulationFunctionTemplate implements SqmFunctionTemplate {
	/**
	 * The default {@code ltrim} function name
	 */
	public static final String LTRIM = "ltrim";

	/**
	 * The default {@code rtrim} function name
	 */
	public static final String RTRIM = "rtrim";

	/**
	 * The default {@code replace} function name
	 */
	public static final String REPLACE = "replace";

	/**
	 * The placeholder used to represent whitespace
	 */
	public static final String TRIM_CHAR_PLACEHOLDER = "${space}$";

	private final String ltrimFunctionName;
	private final String rtrimFunctionName;
	private final String replaceFunctionName;

	/**
	 * Constructs a new AnsiTrimEmulationFunction using {@link #LTRIM}, {@link #RTRIM}, and {@link #REPLACE}
	 * respectively.
	 *
	 * @see #AnsiTrimEmulationFunctionTemplate(String,String,String)
	 */
	public AnsiTrimEmulationFunctionTemplate() {
		this( LTRIM, RTRIM, REPLACE );
	}

	/**
	 * Constructs a <tt>trim()</tt> emulation function definition using the specified function calls.
	 *
	 * @param ltrimFunctionName The <tt>left trim</tt> function to use.
	 * @param rtrimFunctionName The <tt>right trim</tt> function to use.
	 * @param replaceFunctionName The <tt>replace</tt> function to use.
	 */
	public AnsiTrimEmulationFunctionTemplate(String ltrimFunctionName, String rtrimFunctionName, String replaceFunctionName) {
		this.ltrimFunctionName = ltrimFunctionName;
		this.rtrimFunctionName = rtrimFunctionName;
		this.replaceFunctionName = replaceFunctionName;
	}

	@Override
	public SqmFunction makeSqmFunctionExpression(
            SessionFactoryImplementor sessionFactory,
			List<SqmExpression> arguments,
            AllowableFunctionReturnType impliedResultType) {
		final TrimSpecification specification = ( (TrimSpecificationExpressionWrapper) arguments.get( 0 ) ).getSpecification();
		final SqmLiteralCharacter trimCharacterExpr = (SqmLiteralCharacter) arguments.get( 1 );
		final SqmExpression sourceExpr = arguments.get( 1 );

		// NOTE we assume here that the specific ltrim/rtrim/replace function names do not need additional resolution
		//		against the registry!

		switch ( specification ) {
			case LEADING: {
				return trimLeading( sessionFactory, trimCharacterExpr, sourceExpr );
			}
			case TRAILING: {
				return trimTrailing( sessionFactory, trimCharacterExpr, sourceExpr );
			}
			default: {
				return trimBoth( sessionFactory, trimCharacterExpr, sourceExpr );
			}
		}
	}

	private SqmFunction trimLeading(
			SessionFactoryImplementor sessionFactory,
			SqmLiteralCharacter trimChar,
			SqmExpression source) {
		if ( trimChar.getLiteralValue() == ' ' ) {
			return trimLeadingSpaces( sessionFactory, source );
		}
		else {
			return trimLeadingNonSpaces( sessionFactory, trimChar, source );
		}
	}

	private SqmFunction trimLeadingSpaces(SessionFactoryImplementor sessionFactory, SqmExpression source) {
		return ltrim( sessionFactory, source );
	}

	private SqmFunction trimLeadingNonSpaces(SessionFactoryImplementor sessionFactory, SqmLiteralCharacter trimChar, SqmExpression source) {
		final SqmLiteralCharacter space = charExpr( sessionFactory, ' ' );
		final SqmLiteralString placeholder = placeholder( sessionFactory );

		// replace all the '${space}$' text with space chars
		return replace(
				sessionFactory,
				// replace all space chars with the replacement char
				replace(
						sessionFactory,
						// perform left-trimming
						ltrim(
								sessionFactory,
								// replace all the actual replacement chars with space chars
								replace(
										sessionFactory,
										// replace all space chars with the text '${space}$'
										replace(
												sessionFactory,
												source,
												space,
												placeholder
										),
										space,
										placeholder
								)
						),
						space,
						trimChar
				),
				placeholder,
				space
		);
	}

	private SqmFunction trimTrailing(
			SessionFactoryImplementor sessionFactory,
			SqmLiteralCharacter trimChar,
			SqmExpression source) {
		if ( trimChar.getLiteralValue() == ' ' ) {
			return trimTrailingSpaces( sessionFactory, source );
		}
		else {
			return trimTrailingNonSpaces( sessionFactory, trimChar, source );
		}
	}

	private SqmFunction trimTrailingSpaces(SessionFactoryImplementor sessionFactory, SqmExpression sourceExpr) {
		return rtrim( sessionFactory, sourceExpr );
	}

	private SqmFunction trimTrailingNonSpaces(
			SessionFactoryImplementor sessionFactory,
			SqmLiteralCharacter trimChar,
			SqmExpression source) {
		final SqmLiteralCharacter space = charExpr( sessionFactory, ' ' );
		final SqmLiteralString placeholder = placeholder( sessionFactory );

		// replace all the '${space}$' text with space chars
		return replace(
				sessionFactory,
				// replace all space chars with the replacement char
				replace(
						sessionFactory,
						// perform right-trimming
						rtrim(
								sessionFactory,
								// replace all the actual replacement chars with space chars
								replace(
										sessionFactory,
										// replace all space chars with the text '${space}$'
										replace(
												sessionFactory,
												source,
												space,
												placeholder
										),
										space,
										placeholder
								)
						),
						space,
						trimChar
				),
				placeholder,
				space
		);
	}

	private SqmFunction trimBoth(
			SessionFactoryImplementor sessionFactory,
			SqmLiteralCharacter trimCharacterExpr,
			SqmExpression sourceExpr) {
		// BOTH
		if ( trimCharacterExpr.getLiteralValue() == ' ' ) {
			return trimBothSpaces( sessionFactory, sourceExpr );
		}
		else {
			return trimBothNonSpaces( sessionFactory, trimCharacterExpr, sourceExpr );
		}
	}

	private SqmFunction trimBothSpaces(SessionFactoryImplementor sessionFactory, SqmExpression sourceExpr) {
		return ltrim( sessionFactory, rtrim( sessionFactory, sourceExpr ) );
	}

	private SqmFunction trimBothNonSpaces(SessionFactoryImplementor sessionFactory, SqmLiteralCharacter trimChar, SqmExpression source) {
		final SqmLiteralCharacter space = charExpr( sessionFactory, ' ' );
		final SqmLiteralString placeholder = placeholder( sessionFactory );

		// replace all the '${space}$' text with space chars
		return replace(
				sessionFactory,
				// replace all space chars with the replacement char
				replace(
						sessionFactory,
						// perform left-trimming (that removes any of the space chars we just added which occur at the beginning of the text)
						ltrim(
								sessionFactory,
								// perform right-trimming
								rtrim(
										sessionFactory,
										// replace all the actual replacement chars with space chars
										replace(
												sessionFactory,
												// replace all space chars with the text '${space}$'
												replace(
														sessionFactory,
														source,
														space,
														placeholder
												),
												space,
												placeholder
										)
								)
						),
						space,
						trimChar
				),
				placeholder,
				space
		);
	}

	protected SqmFunction replace(SessionFactoryImplementor sessionFactory, SqmExpression source, SqmExpression searchPattern, SqmExpression replacement) {
		return function(
				sessionFactory,
				replaceFunctionName,
				source,
				searchPattern,
				replacement
		);
	}

	protected SqmFunction rtrim(SessionFactoryImplementor sessionFactory, SqmExpression source) {
		return function( sessionFactory, rtrimFunctionName, source );
	}

	protected SqmFunction ltrim(SessionFactoryImplementor sessionFactory, SqmExpression source) {
		return function( sessionFactory, ltrimFunctionName, source );
	}

	private static SqmGenericFunction function(SessionFactoryImplementor sessionFactory, String name, SqmExpression... arguments) {
		return new SqmGenericFunction(
				sessionFactory,
				name,
				StandardSpiBasicTypes.STRING,
				Arrays.asList( arguments )
		);
	}

	protected final SqmLiteralString placeholder(SessionFactoryImplementor sessionFactory) {
		return new SqmLiteralString( sessionFactory, TRIM_CHAR_PLACEHOLDER, StandardSpiBasicTypes.STRING );
	}

	protected SqmLiteralCharacter charExpr(SessionFactoryImplementor sessionFactory, char trimChar) {
		return new SqmLiteralCharacter( sessionFactory, trimChar, StandardSpiBasicTypes.CHARACTER );
	}


}

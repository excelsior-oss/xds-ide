package com.excelsior.xds.parser.internal.modula.symbol.reference;

import com.excelsior.xds.parser.modula.symbol.IInvalidModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;

/**
 * @author lsa80
 *
 * @param <T>
 */
final class ModulaSymbolReference<T extends IModulaSymbol> implements IModulaSymbolReference<T> {
        private final Class<?> symbolClass;
        
        private final IModulaSymbolReference<IModuleSymbol> moduleReference;
        private final ReferenceLocation referenceLocation;
        
//        private final T symbol;  // TODO: symbol reference debug only
        
        ModulaSymbolReference(IModuleSymbol definingModule, T symbol) {
//            this.symbol = symbol;
            symbolClass = symbol.getClass();
            
            moduleReference = ReferenceFactory.createModuleReference(definingModule);
            
            IReferenceResolver referenceResolver = InternalReferenceUtils.getReferenceResolver(definingModule);
            if (referenceResolver != null) {
                referenceLocation = referenceResolver.createReferenceLocation(symbol);
            }
            else{
                referenceLocation = null;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public T resolve() {
        	if (referenceLocation == null) {
                return null;
            }
        	
            IModuleSymbol moduleSymbol = moduleReference.resolve();
            if (moduleSymbol == null) {
                return null;
            }
            
            IReferenceResolver referenceResolver = InternalReferenceUtils.getReferenceResolver(moduleSymbol);
            if (referenceResolver == null) {
                return null;
            }
            
            IModulaSymbol symbol = referenceResolver.resolve(referenceLocation);
            if (symbol == null) {
                return null;
            }
            
            //  if expected class is IInvalidModulaSymbol - then we are dealing with invalid symbol => dont try to check symbolClass
            if (!isInvalidSymbolClass(symbolClass) && !symbolClass.isAssignableFrom(symbol.getClass())) {
                return null;
            }
            
            return (T) symbol;
        }
        
        private static boolean isInvalidSymbolClass(Class<?> c) {
        	return IInvalidModulaSymbol.class.isAssignableFrom(c);
        }
        
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime
                    * result
                    + ((moduleReference == null) ? 0 : moduleReference
                            .hashCode());
            result = prime
                    * result
                    + ((referenceLocation == null) ? 0 : referenceLocation
                            .hashCode());
            result = prime * result
                    + ((symbolClass == null) ? 0 : symbolClass.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            @SuppressWarnings("unchecked")
            ModulaSymbolReference<T> other = (ModulaSymbolReference<T>) obj;
            if (moduleReference == null) {
                if (other.moduleReference != null)
                    return false;
            } else if (!moduleReference.equals(other.moduleReference))
                return false;
            if (referenceLocation == null) {
                if (other.referenceLocation != null)
                    return false;
            } else if (!referenceLocation.equals(other.referenceLocation))
                return false;
            if (symbolClass == null) {
                if (other.symbolClass != null)
                    return false;
            } else if (!symbolClass.equals(other.symbolClass))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "ModulaSymbolReference [symbolClass=" + symbolClass
                    + ", moduleReference=" + moduleReference
                    + ", referenceLocation=" + referenceLocation + "]";
        }
    }
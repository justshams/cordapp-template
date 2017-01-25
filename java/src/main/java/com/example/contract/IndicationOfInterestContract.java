package com.example.contract;

import com.example.model.IndicationOfInterest;
import kotlin.Unit;
import net.corda.core.Utils;
import net.corda.core.contracts.*;
import net.corda.core.contracts.TransactionForContract.InOutGroup;
import net.corda.core.contracts.clauses.*;
import net.corda.core.crypto.SecureHash;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static kotlin.collections.CollectionsKt.single;
import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;
/**
 * Created by Shams on 24-01-2017.
 */
public class IndicationOfInterestContract implements Contract {
    /**
     * This is a reference to the underlying legal contract template and associated parameters.
     */
    private final SecureHash legalContractReference = SecureHash.sha256("purchase order contract template and params");

    @Override public final SecureHash getLegalContractReference() { return legalContractReference; }

    /**
     * Filters the command list by type, party and public key all at once.
     */
    private List<AuthenticatedObject<IndicationOfInterestContract.Commands>> extractCommands(TransactionForContract tx) {
        return tx.getCommands()
                .stream()
                .filter(command -> command.getValue() instanceof IndicationOfInterestContract.Commands)
                .map(command -> new AuthenticatedObject<>(
                        command.getSigners(),
                        command.getSigningParties(),
                        (IndicationOfInterestContract.Commands) command.getValue()))
                .collect(toList());
    }

    /**
     * The AllComposition() clause mandates that all specified clauses clauses (in this case [Timestamped] and [Group])
     * must be executed and valid for a transaction involving this type of contract to be valid.
     */
    @Override
    public void verify(TransactionForContract tx) {
        ClauseVerifier.verifyClause(
                tx,
                new AllComposition<>(new IndicationOfInterestContract.Clauses.Timestamp(), new IndicationOfInterestContract.Clauses.Group()),
                extractCommands(tx));
    }

    /**
     * Currently this contract only implements one command. If you wish to add further commands to perhaps Amend() or
     * Cancel() a purchase order, you would add them here. You would then need to add associated clauses to handle
     * transaction verification for the new commands.
     */
    public interface Commands extends CommandData {
        class Place implements IssueCommand, IndicationOfInterestContract.Commands {
            private final long nonce = Utils.random63BitValue();
            @Override public long getNonce() { return nonce; }
        }
    }

    /**
     * This is where we implement our clauses.
     */
    public interface Clauses {
        /**
         * Checks for the existence of a timestamp.
         */
        class Timestamp extends Clause<ContractState, IndicationOfInterestContract.Commands, Unit> {
            @Override public Set<IndicationOfInterestContract.Commands> verify(TransactionForContract tx,
                                                                        List<? extends ContractState> inputs,
                                                                        List<? extends ContractState> outputs,
                                                                        List<? extends AuthenticatedObject<? extends IndicationOfInterestContract.Commands>> commands,
                                                                        Unit groupingKey) {

                requireNonNull(tx.getTimestamp(), "must be timestamped");

                // We return an empty set because we don't process any commands
                return Collections.emptySet();
            }
        }

        // If you add additional clauses, make sure to reference them within the 'FirstComposition()' clause.
        class Group extends GroupClauseVerifier<IndicationOfInterestState, IndicationOfInterestContract.Commands, UniqueIdentifier> {
            public Group() { super(new FirstComposition<>(new IndicationOfInterestContract.Clauses.Place())); }

            @Override public List<TransactionForContract.InOutGroup<IndicationOfInterestState, UniqueIdentifier>> groupStates(TransactionForContract tx) {
                // Group by purchase order linearId for in/out states.
                return tx.groupStates(IndicationOfInterestState.class, IndicationOfInterestState::getLinearId);
            }
        }

        /**
         * Checks various requirements for the placement of a purchase order.
         */
        class Place extends Clause<IndicationOfInterestState, IndicationOfInterestContract.Commands, UniqueIdentifier> {
            @Override public Set<Class<? extends CommandData>> getRequiredCommands() {
                return Collections.singleton(IndicationOfInterestContract.Commands.Place.class);
            }

            @Override public Set<IndicationOfInterestContract.Commands> verify(TransactionForContract tx,
                                                                        List<? extends IndicationOfInterestState> inputs,
                                                                        List<? extends IndicationOfInterestState> outputs,
                                                                        List<? extends AuthenticatedObject<? extends IndicationOfInterestContract.Commands>> commands,
                                                                        UniqueIdentifier groupingKey)
            {
                final AuthenticatedObject<IndicationOfInterestContract.Commands.Place> command = requireSingleCommand(tx.getCommands(), IndicationOfInterestContract.Commands.Place.class);
                final IndicationOfInterestState out = single(outputs);
                final Instant time = tx.getTimestamp().getMidpoint();

                requireThat(require -> {
                    // Generic constraints around generation of the issue purchase order transaction.
                    require.by("No inputs should be consumed when issuing a IoI order.",
                            inputs.isEmpty());
                    require.by("Only one output state should be created for each group.",
                            outputs.size() == 1);
                    require.by("The buyer and the seller cannot be the same entity.",
                            !out.getBuyer().equals(out.getSeller()));
                    require.by("All of the participants must be signers.",
                            command.getSigners().containsAll(out.getParticipants()));

                    // IoI order specific constraints.
                    //require.by("We only deliver to the UK.",
                    //        out.getPurchaseOrder().getDeliveryAddress().getCountry().equals("UK"));
                    require.by("You must commit at least one.",
                            out.getIndicationOfInterest().getQuantity()>0);
                    require.by("Your price has ot be greater than zero.",
                            out.getIndicationOfInterest().getPrice()>0);

                    return null;
                });

                return Collections.singleton(command.getValue());
            }
        }
    }
}
